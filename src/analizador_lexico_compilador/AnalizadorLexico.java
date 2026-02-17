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
            TablaSimbolos tablasimbolos = new TablaSimbolos();
            Validador validador = new Validador();

            //Mientras la cadena tenga datos, se lee por línea
            while ((cadena = lector.readLine()) != null) {
                System.out.println("Linea " + linenum + ": " + cadena);

                //Tokenizar las líneas leídas
                StringTokenizer tokenizer = new StringTokenizer(cadena);

                //Clasificar los tokens
                while (tokenizer.hasMoreTokens()) {
                    String palabra = tokenizer.nextToken();

                    TablaSimbolos.tokentype type = tablasimbolos.Clasificar(palabra);
                    
                    validador.ValidarReservadas(palabra, type, linenum);

                    if (type != null) {
                        switch (type) {
                            case Reservada:
                                System.out.println("Linea " + linenum + ": " + palabra + " es una palabra RESERVADA");
                                break;

                            case Identificador:
                                System.out.println("Linea " + linenum + ": " + palabra + " es un IDENTIFICADOR");
                                break;
                        }
                    } else {
                        System.out.println("Linea " + linenum + ": " + palabra + " no esta detectada");
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
