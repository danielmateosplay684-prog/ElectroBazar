# Sistema de Sesiones de Caja y Pin-to-Sell

Se ha implementado un sistema profesional de gestión de efectivo y seguridad para el TPV, cumpliendo con los estándares de auditoría y control de pérdidas.

## Componentes Implementados

### 1. Gestión de Sesiones (`CashSession`)
Se ha introducido la entidad `CashSession` para rastrear cada turno físico de caja de forma independiente a los cierres diarios contables.
- **Campos principales:** Trabajador de apertura, fechas, fondo inicial, efectivo esperado (calculado por el sistema) y efectivo real (contado por el empleado).
- **Estado:** Seguimiento de sesiones abiertas/cerradas.

### 2. Seguridad "Pin-to-Sell"
Se ha reforzado la seguridad en el punto de venta:
- **PIN de 4 dígitos:** Cada `Worker` dispone ahora de un código PIN obligatorio para operar el TPV.
- **Bloqueo Automático:** Tras completar cada venta, el TPV se bloquea automáticamente mediante un "overlay" que requiere la verificación del PIN para continuar vendiendo. Esto garantiza que cada ticket esté vinculado al responsable real.

### 3. Flujo de Ventas Vinculado
Todas las ventas procesadas a través del `SaleService` ahora:
- Requieren una `CashSession` abierta obligatoriamente.
- Se vinculan automáticamente a la sesión activa.
- Actualizan el `expectedCash` de la sesión en tiempo real (Suma de efectivo + fondo inicial - devoluciones).

### 4. Cierre Ciego (Audit-Ready)
El proceso de cierre de caja se ha rediseñado para ser "ciego":
- El empleado introduce el total contado en el cajón sin conocer la cifra que el sistema espera.
- La discrepancia se calcula en el servidor, evitando posibles manipulaciones o ajustes manuales "al vuelo" para cuadrar la caja.

---

## Archivos Clave

- **Modelo:** `CashSession.java`, `Worker.java` (campo `pinCode`).
- **Lógica:** `CashSessionServiceImpl.java`, `SaleServiceImpl.java` (integración de sesiones).
- **Frontend:**
    - `index.html`: Nueva interfaz de bloqueo PIN y actualización de formularios de apertura/cierre.
    - `tpv-main.js`: Lógica de AJAX para ventas y control de estado de bloqueo.
- **REST API:** `WorkerApiRestController.java` (endpoint `/verify-pin`).

## Cómo probar el sistema
1.  **Configura un PIN:** Asegúrate de que los trabajadores tengan un PIN de 4 dígitos asignado.
2.  **Apertura:** Al entrar al TPV, abre una sesión con el fondo inicial.
3.  **Venta:** Realiza una venta. Verás que al finalizar, aparece una pantalla de bloqueo.
4.  **Cierre:** Al final del turno, realiza el "Cierre de Caja". Solo introduce el dinero físico contado. El sistema te informará del descuadre si lo hubiera.
