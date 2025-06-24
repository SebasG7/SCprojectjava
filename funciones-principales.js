// ARCHIVO TEMPORAL PARA PROBAR LAS FUNCIONES PRINCIPALES
// Copiar este código al principio del script en ventas.html

// ========== DEFINICIONES DE FUNCIONES GLOBALES (ANTES DE QUE SE USEN) ==========

// Global variables
window.contadorProductos = 0;
window.productosData = {};
window.productosDataConIva = {};

// ========== FUNCIONES PRINCIPALES QUE SE USAN EN onclick ==========

// Función para buscar productos con AJAX - DEFINIDA TEMPRANO
window.buscarProductos = function(busquedaParam, categoriaParam) {
    console.log('buscarProductos ejecutada');
    // Mostrar indicador de carga
    const loadingIndicator = document.createElement('div');
    loadingIndicator.className = 'text-center p-3';
    loadingIndicator.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Cargando...</span></div><p class="mt-2">Buscando productos...</p>';
    
    const container = document.getElementById('productosListaContainer');
    container.innerHTML = '';
    container.appendChild(loadingIndicator);
    
    // Obtener valores de búsqueda
    const busqueda = busquedaParam !== undefined ? busquedaParam : document.getElementById('busqueda').value;
    const categoria = categoriaParam !== undefined ? categoriaParam : document.getElementById('categoria').value;
    
    // Actualizar campos de búsqueda si se proporcionan parámetros
    if (busquedaParam !== undefined) {
        document.getElementById('busqueda').value = busqueda;
    }
    if (categoriaParam !== undefined) {
        document.getElementById('categoria').value = categoria;
    }
    
    // Construir URL para la solicitud AJAX
    let url = '/test-ventas/buscar-ajax?';
    if (busqueda) {
        url += 'busqueda=' + encodeURIComponent(busqueda) + '&';
    }
    if (categoria) {
        url += 'categoria=' + encodeURIComponent(categoria);
    }
    
    // Realizar la solicitud AJAX
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error en la búsqueda: ' + response.status);
            }
            return response.text();
        })
        .then(html => {
            // Actualizar contenedor con resultados
            container.innerHTML = html;
        })
        .catch(error => {
            console.error('Error en búsqueda AJAX:', error);
            container.innerHTML = `
                <div class="alert alert-danger alert-custom alert-danger-custom">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    Error al buscar productos: ${error.message}
                </div>
            `;
        });
};

// Función para limpiar filtros - DEFINIDA TEMPRANO  
window.limpiarFiltros = function() {
    console.log('limpiarFiltros ejecutada');
    
    // Limpiar campos de búsqueda
    document.getElementById('busqueda').value = '';
    document.getElementById('categoria').value = '';
    
    // Realizar búsqueda con filtros vacíos (mostrará todos los productos)
    window.buscarProductos('', '');
};

// Función para limpiar carrito completo - DEFINIDA TEMPRANO
window.limpiarCarritoCompleto = function() {
    console.log('limpiarCarritoCompleto ejecutada');
    // Confirmar con el usuario
    Swal.fire({
        title: '¿Estás seguro?',
        text: "Se perderán todos los productos y correos agregados al carrito.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3a506b',
        cancelButtonColor: '#dc3545',
        confirmButtonText: 'Sí, limpiar',
        cancelButtonText: 'Cancelar'
    }).then((result) => {
        if (result.isConfirmed) {
            // Limpiar localStorage
            localStorage.removeItem('carritoVentas');
            
            // Resetear formulario
            window.contadorProductos = 0;
            
            // Limpiar nombre del cliente
            const nombreClienteInput = document.getElementById('nombreCliente');
            if (nombreClienteInput) {
                nombreClienteInput.value = '';
            }
            
            // Limpiar correos (dejar solo el primero)
            const container = document.getElementById('correos-container');
            if (container) {
                container.innerHTML = `
                    <div class="input-group input-group-custom mb-3">
                        <input type="email" name="correos" class="form-control form-control-custom" 
                               placeholder="correo@cliente.com">
                        <button type="button" class="btn btn-success-custom btn-custom" onclick="window.agregarCorreo()">
                            <i class="bi bi-plus-lg"></i> Agregar
                        </button>
                    </div>
                `;
            }
            
            // Limpiar todos los productos y restaurar mensaje informativo
            const productosContainer = document.getElementById("productosContainer");
            if (productosContainer) {
                productosContainer.innerHTML = `
                    <div class="alert alert-info alert-custom alert-info-custom">
                        <i class="bi bi-info-circle me-2"></i>
                        Use la búsqueda de productos arriba para agregar productos al carrito.
                    </div>
                `;
            }
            
            // Recalcular total (debería ser 0)
            if (typeof window.calcularTotal === 'function') {
                window.calcularTotal();
            }
            
            Swal.fire({
                title: 'Carrito limpiado',
                text: 'Se ha limpiado el carrito exitosamente.',
                icon: 'success',
                confirmButtonColor: '#3a506b'
            });
        }
    });
};

// Función para agregar producto desde la grilla - DEFINIDA TEMPRANO
window.agregarProductoDesdeGrid = function(button) {
    console.log('agregarProductoDesdeGrid ejecutada');
    try {
        // Obtener datos del producto desde el botón
        const productoId = button.getAttribute('data-id');
        const nombre = button.getAttribute('data-nombre');
        const precioBase = parseFloat(button.getAttribute('data-precio'));
        const precioConIva = parseFloat(button.getAttribute('data-precio-con-iva'));
        const iva = parseFloat(button.getAttribute('data-iva')) || 0;
        const codigo = button.getAttribute('data-codigo');
        const isVencido = button.getAttribute('data-vencido') === 'true';
        
        if (!productoId || !nombre || isNaN(precioBase) || isNaN(precioConIva)) {
            console.error('Datos de producto incompletos:', productoId, nombre, precioBase, precioConIva);
            return;
        }
        
        // Validar que el producto no esté vencido
        if (isVencido) {
            Swal.fire({
                title: 'Producto vencido',
                text: `No se puede agregar ${nombre} al carrito porque está vencido`,
                icon: 'error',
                confirmButtonText: 'Entendido',
                confirmButtonColor: '#dc3545'
            });
            return;
        }
        
        console.log(`Agregando producto desde grid: ${nombre}, precio base: ${precioBase}, precio con IVA: ${precioConIva}`);
        
        // Buscar el contenedor de productos
        const container = document.getElementById("productosContainer");
        
        if (!container) {
            console.error('No se encontró el contenedor de productos');
            return;
        }
        
        // Verificar si el producto ya existe en el carrito
        const productosExistentes = container.querySelectorAll(".producto-item");
        let productoExistente = null;
        
        for (const item of productosExistentes) {
            const select = item.querySelector("select");
            if (select && select.value === productoId) {
                productoExistente = item;
                break;
            }
        }
        
        if (productoExistente) {
            // Producto ya existe, incrementar cantidad
            const cantidadInput = productoExistente.querySelector("input[name*='cantidad']");
            if (cantidadInput) {
                const cantidadActual = parseInt(cantidadInput.value) || 0;
                cantidadInput.value = cantidadActual + 1;
                if (typeof window.calcularSubtotal === 'function') {
                    window.calcularSubtotal(productoExistente);
                }
            }
        } else {
            // Producto nuevo, crear elemento
            // Limpiar mensaje informativo si existe
            const alertInfo = container.querySelector('.alert-info');
            if (alertInfo) {
                alertInfo.remove();
            }
            
            if (typeof window.crearProductoItem === 'function') {
                const nuevoProducto = window.crearProductoItem(productoId, nombre, precioBase, precioConIva, iva, 1, isVencido);
                container.appendChild(nuevoProducto);
            
                // Actualizar datos de productos
                window.productosData[productoId] = precioBase;
                window.productosDataConIva[productoId] = precioConIva;
            }
        }
        
        // Recalcular total
        if (typeof window.calcularTotal === 'function') {
            window.calcularTotal();
        }
        
        // Mostrar notificación
        Swal.fire({
            title: 'Producto agregado',
            text: `${nombre} se ha agregado al carrito`,
            icon: 'success',
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000
        });
        
        // Guardar carrito
        if (typeof window.guardarCarrito === 'function') {
            window.guardarCarrito();
        }
        
        // Hacer scroll al carrito para que el usuario vea el cambio
        container.scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error al agregar producto desde grid:', error);
        Swal.fire({
            title: 'Error',
            text: 'No se pudo agregar el producto al carrito',
            icon: 'error',
            confirmButtonColor: '#3a506b'
        });
    }
};

// Función básica para guardar carrito - SIMPLE
window.guardarCarrito = function() {
    console.log('guardarCarrito ejecutada');
    try {
        const productos = [];
        const correos = [];
        
        // Guardar productos del carrito
        document.querySelectorAll('.producto-item').forEach((item, index) => {
            const select = item.querySelector('select');
            const cantidadInput = item.querySelector("input[name*='cantidad']");
            const precioInput = item.querySelector("input[name*='precioUnitario']");
            
            if (select && select.value && cantidadInput && cantidadInput.value && precioInput && precioInput.value) {
                const selectedOption = select.querySelector('option:checked');
                productos.push({
                    productoId: select.value,
                    cantidad: cantidadInput.value,
                    precio: precioInput.value,
                    nombre: selectedOption ? selectedOption.textContent : 'Producto'
                });
            }
        });
        
        // Guardar correos
        document.querySelectorAll('input[name="correos"]').forEach(input => {
            if (input.value.trim()) {
                correos.push(input.value.trim());
            }
        });
        
        // Guardar nombre del cliente
        const nombreClienteInput = document.getElementById('nombreCliente');
        const nombreCliente = nombreClienteInput ? nombreClienteInput.value.trim() : '';
        
        const carritoData = {
            productos: productos,
            correos: correos,
            nombreCliente: nombreCliente,
            contador: window.contadorProductos,
            timestamp: Date.now()
        };
        
        localStorage.setItem('carritoVentas', JSON.stringify(carritoData));
        console.log('Carrito guardado exitosamente');
        return true;
    } catch (error) {
        console.error('Error al guardar carrito:', error);
        return false;
    }
};

// Función para agregar correos - DEFINIDA TEMPRANO
window.agregarCorreo = function() {
    console.log('agregarCorreo ejecutada');
    try {
        const container = document.getElementById('correos-container');
        if (!container) {
            console.error('Container de correos no encontrado');
            return;
        }
        
        const nuevoCampo = document.createElement('div');
        nuevoCampo.classList.add('input-group', 'input-group-custom', 'mb-3');
        nuevoCampo.innerHTML = `
            <input type="email" name="correos" class="form-control form-control-custom" 
                   placeholder="correo@cliente.com">
            <button type="button" class="btn btn-danger-custom btn-custom" onclick="this.parentElement.remove(); window.guardarCarrito();">
                <i class="bi bi-dash-lg"></i> Quitar
            </button>
        `;
        container.appendChild(nuevoCampo);
        window.guardarCarrito();
    } catch (error) {
        console.error('Error al agregar correo:', error);
    }
};

// Función básica para calcular total - SIMPLE
window.calcularTotal = function() {
    try {
        let total = 0;
        let productosValidos = 0;
        
        document.querySelectorAll('.producto-item').forEach(item => {
            const select = item.querySelector('select');
            const cantidad = item.querySelector("input[name*='cantidad']");
            const precio = item.querySelector("input[name*='precioUnitario']");
            
            if (select && select.value && cantidad && cantidad.value && precio && precio.value) {
                const subtotal = parseFloat(cantidad.value) * parseFloat(precio.value);
                total += subtotal;
                productosValidos++;
                
                // Actualizar subtotal individual si existe
                const subtotalInput = item.querySelector('.subtotal');
                if (subtotalInput) {
                    subtotalInput.value = subtotal.toFixed(2);
                }
            }
        });
        
        // Actualizar total
        const totalElement = document.getElementById("totalVenta");
        if (totalElement) {
            totalElement.textContent = total.toFixed(2);
        }
        
        // Actualizar contador de productos
        const contadorElement = document.getElementById("contadorProductos");
        if (contadorElement) {
            contadorElement.textContent = productosValidos;
        }
    } catch (error) {
        console.error('Error al calcular total:', error);
    }
};

console.log('=== FUNCIONES PRINCIPALES CARGADAS ===');
console.log('- buscarProductos:', typeof window.buscarProductos);
console.log('- limpiarFiltros:', typeof window.limpiarFiltros);
console.log('- agregarProductoDesdeGrid:', typeof window.agregarProductoDesdeGrid);
console.log('- limpiarCarritoCompleto:', typeof window.limpiarCarritoCompleto);
console.log('- guardarCarrito:', typeof window.guardarCarrito);
console.log('- agregarCorreo:', typeof window.agregarCorreo);
console.log('- calcularTotal:', typeof window.calcularTotal);
