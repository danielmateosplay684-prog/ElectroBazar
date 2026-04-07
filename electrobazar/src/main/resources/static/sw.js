const CACHE_NAME = 'tpv-v3';
const ASSETS = [
    '/tpv',
    '/css/tpv/tpv-main.css',
    '/js/tpv/tpv-main.js',
    '/icons/favicon.svg',
    '/icons/favicon-light.svg',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css',
    'https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js',
    'https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css',
    'https://fonts.googleapis.com/css2?family=Barlow:wght@400;500;600;700&family=Barlow+Condensed:wght@600;700&display=swap'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(ASSETS);
        })
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(
                keys.filter(key => key !== CACHE_NAME)
                    .map(key => caches.delete(key))
            );
        })
    );
});

self.addEventListener('fetch', event => {
    // 1. BYPASS REST API: Ignorar cualquier petición que contenga /api/ para que el SW no interfiera
    const url = new URL(event.request.url);
    if (url.pathname.includes('/api/')) {
        return; // Deja que la petición vaya directamente a la red sin capturarla
    }

    // 2. Only intercept GET requests for static content or navigation
    if (event.request.method !== 'GET') return;

    // Strategy: Network First for the main navigation (/tpv)
    if (url.pathname === '/tpv' || url.pathname.startsWith('/admin') || url.pathname.startsWith('/tpv/receipt') || url.pathname.startsWith('/tpv/return-receipt') || event.request.mode === 'navigate') {
        event.respondWith(
            fetch(event.request)
                .then(response => {
                    // Only cache successful GET responses that are not ADMIN pages
                    if (response.ok && response.status === 200 && !url.pathname.startsWith('/admin')) {
                        const copy = response.clone();
                        caches.open(CACHE_NAME).then(cache => {
                            cache.put(event.request, copy).catch(() => {});
                        });
                    }
                    return response;
                })
                .catch(err => {
                    return caches.match(event.request).then(r => {
                        if (r) return r;
                        throw err;
                    });
                })
        );
        return;
    }

    // Strategy: Cache First for other static assets (CSS, JS, Fonts, etc.)
    event.respondWith(
        caches.match(event.request).then(response => {
            return response || fetch(event.request).then(networkResponse => {
                if (networkResponse.ok && (url.origin === location.origin)) {
                    const copy = networkResponse.clone();
                    caches.open(CACHE_NAME).then(cache => {
                        cache.put(event.request, copy).catch(() => {});
                    });
                }
                return networkResponse;
            }).catch(err => {
                throw err;
            });
        })
    );
});
