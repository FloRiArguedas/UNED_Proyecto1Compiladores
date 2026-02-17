package analizador_lexico_compilador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author fargu
 */
public class AnalizadorLexico {

//DEFINICION DE PALABRAS RESERVADAS
    //Utilizo Set y no una lista, porque es más eficiente para búsquedas y no permite duplicados.
    private static final Set<String> Palabras_Reservadas = new HashSet<>(Arrays.asList("module", "sub", "dim", "as", "if", "then", "elself", "else",
            "function", "return", "while", "end"));

//LECTURA DEL ARCHIVO 
    public void leer_archivo(String rutaarchivo) {

        FileReader archivo;
        // Utilizo BufferedReader para leer línea por línea
        BufferedReader lector;
        String cadena;
        int linenum = 1;

        try {
            //Abro el archivo con la ruta proporcionada por el usuario
            archivo = new FileReader(rutaarchivo);
            lector = new BufferedReader(archivo);

            //Mientras la cadena tenga datos, la leo por línea
            while ((cadena = lector.readLine()) != null) {
                System.out.println("Linea" + linenum + ": " + cadena);

                //Tokenizar las líneas leídas
                StringTokenizer tokenizer = new StringTokenizer(cadena);

                //Verificar los tokens para encontrar las palabras reservadas
                while (tokenizer.hasMoreTokens()) {
                    String palabra = tokenizer.nextToken();

                    //Normalizo palabra para reconocerlas sin importar su escritura
                    if (Palabras_Reservadas.contains(palabra.toLowerCase())) {
                        System.out.println("Linea " + linenum + ": " + palabra + " es RESERVADA");
                    } else {
                        System.out.println("Linea " + linenum + ": " + palabra + " es IDENTIFICADOR u otro");
                    }
                }
                linenum++;
            }

            lector.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
