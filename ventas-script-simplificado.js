    <!-- Scripts -->
    <script>
        // ========== DEFINICIONES DE FUNCIONES GLOBALES (PRINCIPALES) ==========
        
        // Global variables
        window.contadorProductos = 0;
        window.productosData = {};
        window.productosDataConIva = {};

        // ========== FUNCIONES PRINCIPALES QUE SE USAN EN onclick ==========
        
        // Función para buscar productos con AJAX
        window.buscarProductos = function(busquedaParam, categoriaParam) {
            console.log('buscarProductos ejecutada');
            const loadingIndicator = document.createElement('div');
            loadingIndicator.className = 'text-center p-3';
            loadingIndicator.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Cargando...</span></div><p class="mt-2">Buscando productos...</p>';
            
            const container = document.getElementById('productosListaContainer');
            container.innerHTML = '';
            container.appendChild(loadingIndicator);
            
            const busqueda = busquedaParam !== undefined ? busquedaParam : document.getElementById('busqueda').value;
            const categoria = categoriaParam !== undefined ? categoriaParam : document.getElementById('categoria').value;
            
            if (busquedaParam !== undefined) {
                document.getElementById('busqueda').value = busqueda;
            }
            if (categoriaParam !== undefined) {
                document.getElementById('categoria').value = categoria;
            }
            
            let url = '/test-ventas/buscar-ajax?';
            if (busqueda) {
                url += 'busqueda=' + encodeURIComponent(busqueda) + '&';
            }
            if (categoria) {
                url += 'categoria=' + encodeURIComponent(categoria);
            }
            
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Error en la búsqueda: ' + response.status);
                    }
                    return response.text();
                })
                .then(html => {
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

        // Función para limpiar filtros
        window.limpiarFiltros = function() {
            console.log('limpiarFiltros ejecutada');
            document.getElementById('busqueda').value = '';
            document.getElementById('categoria').value = '';
            window.buscarProductos('', '');
        };

        // Función para limpiar carrito completo
        window.limpiarCarritoCompleto = function() {
            console.log('limpiarCarritoCompleto ejecutada');
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
                    localStorage.removeItem('carritoVentas');
                    window.contadorProductos = 0;
                    
                    const nombreClienteInput = document.getElementById('nombreCliente');
                    if (nombreClienteInput) {
                        nombreClienteInput.value = '';
                    }
                    
                    const container = document.getElementById('correos-container');
                    if (container) {
                        container.innerHTML = `
                            <div class="input-group input-group-custom mb-2">
                                <input type="email" name="correos" class="form-control form-control-custom" 
                                       placeholder="correo@cliente.com">
                                <button type="button" class="btn btn-success-custom btn-custom" onclick="window.agregarCorreo()">
                                    <i class="bi bi-plus-lg"></i> Agregar
                                </button>
                            </div>
                        `;
                    }
                    
                    const productosContainer = document.getElementById("productosContainer");
                    if (productosContainer) {
                        productosContainer.innerHTML = `
                            <div class="alert alert-info alert-custom alert-info-custom">
                                <i class="bi bi-info-circle me-2"></i>
                                Use la búsqueda de productos arriba para agregar productos al carrito.
                            </div>
                        `;
                    }
                    
                    window.calcularTotal();
                    
                    Swal.fire({
                        title: 'Carrito limpiado',
                        text: 'Se ha limpiado el carrito exitosamente.',
                        icon: 'success',
                        confirmButtonColor: '#3a506b'
                    });
                }
            });
        };

        // Función para agregar producto desde la grilla
        window.agregarProductoDesdeGrid = function(button) {
            console.log('agregarProductoDesdeGrid ejecutada');
            try {
                const productoId = button.getAttribute('data-id');
                const nombre = button.getAttribute('data-nombre');
                const precioBase = parseFloat(button.getAttribute('data-precio'));
                const precioConIva = parseFloat(button.getAttribute('data-precio-con-iva'));
                const iva = parseFloat(button.getAttribute('data-iva')) || 0;
                const isVencido = button.getAttribute('data-vencido') === 'true';
                
                if (!productoId || !nombre || isNaN(precioBase) || isNaN(precioConIva)) {
                    console.error('Datos de producto incompletos:', productoId, nombre, precioBase, precioConIva);
                    return;
                }
                
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
                        window.calcularSubtotal(productoExistente);
                    }
                } else {
                    // Producto nuevo, crear elemento
                    const alertInfo = container.querySelector('.alert-info');
                    if (alertInfo) {
                        alertInfo.remove();
                    }
                    
                    const nuevoProducto = window.crearProductoItem(productoId, nombre, precioBase, precioConIva, iva, 1, isVencido);
                    container.appendChild(nuevoProducto);
                    
                    window.productosData[productoId] = precioBase;
                    window.productosDataConIva[productoId] = precioConIva;
                }
                
                window.calcularTotal();
                
                Swal.fire({
                    title: 'Producto agregado',
                    text: `${nombre} se ha agregado al carrito`,
                    icon: 'success',
                    toast: true,
                    position: 'top-end',
                    showConfirmButton: false,
                    timer: 3000
                });
                
                window.guardarCarrito();
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

        // Función para agregar correos
        window.agregarCorreo = function() {
            console.log('agregarCorreo ejecutada');
            try {
                const container = document.getElementById('correos-container');
                if (!container) {
                    console.error('Container de correos no encontrado');
                    return;
                }
                
                const nuevoCampo = document.createElement('div');
                nuevoCampo.classList.add('input-group', 'input-group-custom', 'mb-2');
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

        // ========== FUNCIONES AUXILIARES ==========

        // Función para crear un elemento de producto
        window.crearProductoItem = function(productoId, nombre, precioBase, precioConIva, iva, cantidad = 1, isVencido = false) {
            window.contadorProductos++;
            
            const productItem = document.createElement('div');
            productItem.className = 'producto-item mb-3 p-3 border rounded bg-light';
            
            productItem.innerHTML = `
                <div class="row align-items-center">
                    <div class="col-md-3">
                        <label class="form-label form-label-custom">Producto</label>
                        <select name="productos[${window.contadorProductos}].productoId" class="form-select form-control-custom" 
                                onchange="window.actualizarPrecio(this); window.calcularSubtotal(this.closest('.producto-item')); window.guardarCarrito();">
                            <option value="">Seleccione un producto</option>
                            <option value="${productoId}" data-precio="${precioBase}" data-precio-con-iva="${precioConIva}" data-iva="${iva}" data-vencido="${isVencido}" selected>${nombre}</option>
                        </select>
                    </div>
                    <div class="col-md-1">
                        <label class="form-label form-label-custom">Cantidad</label>
                        <input type="number" name="productos[${window.contadorProductos}].cantidad" 
                               class="form-control form-control-custom" min="1" value="${cantidad}"
                               onchange="window.calcularSubtotal(this.closest('.producto-item')); window.guardarCarrito();">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label form-label-custom">Precio Base</label>
                        <input type="number" class="form-control form-control-custom" step="0.01" readonly value="${precioBase.toFixed(2)}">
                    </div>
                    <div class="col-md-1">
                        <label class="form-label form-label-custom">IVA %</label>
                        <input type="number" class="form-control form-control-custom" step="0.01" readonly value="${iva.toFixed(1)}">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label form-label-custom">Precio Unitario (con IVA)</label>
                        <input type="number" name="productos[${window.contadorProductos}].precioUnitario" 
                               class="form-control form-control-custom" step="0.01" readonly value="${precioConIva.toFixed(2)}">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label form-label-custom">Subtotal</label>
                        <input type="number" class="form-control form-control-custom subtotal" readonly value="${(precioConIva * cantidad).toFixed(2)}">
                    </div>
                    <div class="col-md-1 d-flex align-items-end">
                        <button type="button" class="btn btn-danger-custom btn-custom" 
                                onclick="window.eliminarProducto(this.closest('.producto-item'))">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            `;
            
            return productItem;
        };

        // Función para actualizar precio
        window.actualizarPrecio = function(select) {
            try {
                const selectedOption = select.querySelector('option:checked');
                if (selectedOption) {
                    const isVencido = selectedOption.getAttribute('data-vencido') === 'true';
                    if (isVencido) {
                        Swal.fire({
                            title: 'Producto vencido',
                            text: 'No se puede seleccionar este producto porque está vencido',
                            icon: 'error',
                            confirmButtonColor: '#dc3545'
                        });
                        select.value = '';
                        return;
                    }
                }
                
                const precio = window.productosData[select.value] || 0;
                const row = select.closest(".producto-item");
                const precioInput = row.querySelector("input[name*='precioUnitario']");
                
                if (precioInput) {
                    precioInput.value = precio.toFixed(2);
                    window.calcularSubtotal(row);
                    window.guardarCarrito();
                }
            } catch (error) {
                console.error('Error al actualizar precio:', error);
            }
        };

        // Función para calcular subtotal
        window.calcularSubtotal = function(element) {
            try {
                const row = element.classList && element.classList.contains('producto-item') 
                    ? element 
                    : element.closest(".producto-item");
                
                if (!row) {
                    console.error('No se pudo encontrar el contenedor del producto');
                    return;
                }
                
                const cantidadInput = row.querySelector("input[name*='cantidad']");
                const precioUnitarioInput = row.querySelector("input[name*='precioUnitario']");
                const subtotalInput = row.querySelector(".subtotal");
                
                if (cantidadInput && precioUnitarioInput && subtotalInput) {
                    const cantidad = parseFloat(cantidadInput.value) || 0;
                    const precioUnitario = parseFloat(precioUnitarioInput.value) || 0;
                    const subtotal = cantidad * precioUnitario;
                    
                    subtotalInput.value = subtotal.toFixed(2);
                    window.calcularTotal();
                    window.guardarCarrito();
                }
            } catch (error) {
                console.error('Error al calcular subtotal:', error);
            }
        };

        // Función para calcular total
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
                    }
                });
                
                const totalElement = document.getElementById("totalVenta");
                if (totalElement) {
                    totalElement.textContent = total.toFixed(2);
                }
                
                const contadorElement = document.getElementById("contadorProductos");
                if (contadorElement) {
                    contadorElement.textContent = productosValidos;
                }
            } catch (error) {
                console.error('Error al calcular total:', error);
            }
        };

        // Función para eliminar producto
        window.eliminarProducto = function(element) {
            const item = element.closest(".producto-item");
            if (!item) {
                console.error('No se pudo encontrar el producto a eliminar');
                return;
            }
            
            const items = document.querySelectorAll(".producto-item");
            
            if (items.length > 1) {
                item.remove();
                window.calcularTotal();
                window.guardarCarrito();
                
                Swal.fire({
                    title: 'Producto eliminado',
                    text: 'El producto ha sido eliminado del carrito',
                    icon: 'success',
                    toast: true,
                    position: 'top-end',
                    showConfirmButton: false,
                    timer: 2000
                });
            } else {
                Swal.fire({
                    title: '¿Limpiar carrito?',
                    text: 'Este es el único producto en el carrito. ¿Desea limpiar todo el carrito?',
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#3a506b',
                    cancelButtonColor: '#6c757d',
                    confirmButtonText: 'Sí, limpiar carrito',
                    cancelButtonText: 'Cancelar'
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.limpiarCarritoCompleto();
                    }
                });
            }
        };

        // Función para guardar carrito
        window.guardarCarrito = function() {
            try {
                const productos = [];
                const correos = [];
                
                document.querySelectorAll('.producto-item').forEach((item) => {
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
                
                document.querySelectorAll('input[name="correos"]').forEach(input => {
                    if (input.value.trim()) {
                        correos.push(input.value.trim());
                    }
                });
                
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
                return true;
            } catch (error) {
                console.error('Error al guardar carrito:', error);
                return false;
            }
        };

        // ========== INICIALIZACIÓN ==========

        // Validar formulario antes de enviar
        document.addEventListener('DOMContentLoaded', function() {
            console.log('=== VENTAS.HTML - Página cargada ===');
            console.log('Funciones disponibles:');
            console.log('- buscarProductos:', typeof window.buscarProductos);
            console.log('- limpiarFiltros:', typeof window.limpiarFiltros);
            console.log('- agregarProductoDesdeGrid:', typeof window.agregarProductoDesdeGrid);
            console.log('- limpiarCarritoCompleto:', typeof window.limpiarCarritoCompleto);
            console.log('- guardarCarrito:', typeof window.guardarCarrito);
            console.log('- agregarCorreo:', typeof window.agregarCorreo);
            
            // Validar formulario
            const form = document.getElementById('ventaForm');
            if (form) {
                form.addEventListener('submit', function(e) {
                    const productos = document.querySelectorAll('.producto-item');
                    let tieneProductosValidos = false;
                    
                    productos.forEach(producto => {
                        const select = producto.querySelector('select');
                        const cantidad = producto.querySelector("input[name*='cantidad']");
                        
                        if (select && select.value && cantidad && cantidad.value && parseFloat(cantidad.value) > 0) {
                            tieneProductosValidos = true;
                        }
                    });
                    
                    if (!tieneProductosValidos) {
                        e.preventDefault();
                        Swal.fire({
                            icon: 'error',
                            title: 'Error',
                            text: 'Debe agregar al menos un producto válido.',
                            confirmButtonColor: '#3a506b'
                        });
                    } else {
                        localStorage.removeItem('carritoVentas');
                    }
                });
            }
            
            // Event listeners para búsqueda
            const busquedaInput = document.getElementById('busqueda');
            if (busquedaInput) {
                busquedaInput.addEventListener('keypress', function(e) {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        window.buscarProductos();
                    }
                });
            }
            
            // Verificar si hay parámetro de éxito
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('success') === 'true') {
                localStorage.removeItem('carritoVentas');
                Swal.fire({
                    title: '¡Venta exitosa!',
                    text: 'La venta ha sido registrada exitosamente.',
                    icon: 'success',
                    confirmButtonColor: '#3a506b'
                });
            }
            
            console.log('=== INICIALIZACIÓN COMPLETADA ===');
        });
    </script>
