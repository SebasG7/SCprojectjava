import com.scprojectjava2.service.UnidadService;

public class TestAbreviaturaUnica {
    public static void main(String[] args) {
        UnidadService unidadService = new UnidadService();
        
        // Ejemplos de cómo funciona la nueva lógica
        
        // Caso 1: Primera vez creando "kilogramo"
        String abrev1 = unidadService.generarAbreviaturaUnica("kilogramo");
        System.out.println("kilogramo -> " + abrev1); // Resultado: "KIL"
        
        // Simular que "KIL" ya existe
        // Caso 2: Segunda unidad con mismo prefijo
        String abrev2 = unidadService.generarAbreviaturaUnica("kilogramo de peso");
        System.out.println("kilogramo de peso -> " + abrev2); // Resultado: "KIL2"
        
        // Caso 3: Tercera unidad con mismo prefijo
        String abrev3 = unidadService.generarAbreviaturaUnica("kilolitro");
        System.out.println("kilolitro -> " + abrev3); // Resultado: "KIL3"
        
        // Caso 4: Unidad con prefijo diferente
        String abrev4 = unidadService.generarAbreviaturaUnica("metro");
        System.out.println("metro -> " + abrev4); // Resultado: "MET"
        
        // Caso 5: Unidad con nombre corto
        String abrev5 = unidadService.generarAbreviaturaUnica("kg");
        System.out.println("kg -> " + abrev5); // Resultado: "KG"
    }
}

/*
FLUJO DE LA LÓGICA:

1. Se extrae abreviatura base (primeras 3 letras en mayúsculas)
2. Se verifica si ya existe en la base de datos
3. Si NO existe -> se usa la abreviatura base
4. Si SÍ existe -> se agrega "2", "3", "4"... hasta encontrar una única

EJEMPLOS PRÁCTICOS durante carga masiva:

CSV contiene:
- kilogramo    -> Se crea unidad con abreviatura "KIL"
- kilolitro    -> Se crea unidad con abreviatura "KIL2" 
- kilómetro    -> Se crea unidad con abreviatura "KIL3"
- metro        -> Se crea unidad con abreviatura "MET"
- mililitro    -> Se crea unidad con abreviatura "MIL"
- milímetro    -> Se crea unidad con abreviatura "MIL2"

Esto garantiza que NUNCA habrá abreviaturas duplicadas.
*/
