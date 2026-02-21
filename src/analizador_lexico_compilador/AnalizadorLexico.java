package analizador_lexico_compilador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author fargu
 */
public class AnalizadorLexico {

//FUNCION COMPLETA PARA ANALIZAR EL ARCHIVO 
    public void analizar_archivo(String rutaarchivo) {

        String cadena;
        int linenum = 1;
        String LastLine = "";
        int LastLineNum = -1;

        try {
            //Abro el archivo con la ruta proporcionada por el usuario
            FileReader archivo = new FileReader(rutaarchivo);
            BufferedReader lector = new BufferedReader(archivo);
            TablaSimbolos tablasimbolos = new TablaSimbolos();
            Registrador registrador = new Registrador();
            Validador validador = new Validador(registrador);

            //CREACION ARCHIVO LOGS
            String FilelogsPath = registrador.CrearArchivoLogs(rutaarchivo);
            //LIMPIO ARCHIVO LOGS
            registrador.Sobreescribir(FilelogsPath);

            //INICIAR LECTURA DEL ARCHIVO
            while ((cadena = lector.readLine()) != null) {
                //TEMPORAL PARA MOSTRAR EN CONSOLA LAS LINEAS
                System.out.println("Linea " + linenum + ": " + cadena);

                //ESCRIBIR EN ARCHIVO LOGS
                registrador.EscribirLinea(linenum, cadena);

                //VERIFICO QUE LA LINEA NO SEA UN COMENTARIO, PARA PODER VALIDARLA.
                if (!validador.ValidarComentarios(cadena, linenum)) {

                    //TOKENIZAR LINEAS LEIDAS
                    StringTokenizer tokenizer = new StringTokenizer(cadena);

                    //Creo lista para guardar tokens de la linea
                    List<TablaSimbolos.tokentype> CompleteTokensLine = new ArrayList<>();
                    //Creo lista para guardar la linea
                    List<String> CompleteLine = new ArrayList<>();

                    //CLASIFICACION DE TOKENS
                    while (tokenizer.hasMoreTokens()) {
                        String palabra = tokenizer.nextToken();

                        TablaSimbolos.tokentype type = tablasimbolos.Clasificar(palabra);

                        //Añado cada tipo de token a la lista de la linea
                        CompleteTokensLine.add(type);
                        //Añado cada palabra a la lista de la linea
                        CompleteLine.add(palabra);

                        //VALIDACION DE PALABRAS RESERVADAS
                        validador.ValidarReservadas(palabra, type, linenum);

                    }

                    //VALIDACION DE ESTRUCTURA DE MODULE
                    validador.ValidarEstructuraModule(CompleteLine, CompleteTokensLine, linenum, cadena);

                    //VALIDACION DE FORMATOS DIM
                    validador.ValidarDeclaracionDim(CompleteLine, CompleteTokensLine, linenum);

                    //VALIDACION DE CONSOLE.WRITELINE
                    validador.ValidarSentenciasCWL(CompleteLine, linenum);

                    //VALIDACION END MODULE
                    validador.ValidarEndModule(CompleteLine, cadena, linenum);
                }

                // PASO A LA SIGUIENTE LINEA DEL ARCHIVO
                linenum++;
            }

            //Cierro los archivos
            registrador.cerrarLogs();
            lector.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void MostrarTypeConsola(TablaSimbolos.tokentype type, int linenum, String palabra) {

        //MOSTRAR EL CONSOLA EL TIPO DE TOKEN
        if (type != null) {
            switch (type) {
                case Reservada:
                    System.out.println("Linea " + linenum + ": " + palabra + " es una palabra RESERVADA");
                    break;

                case Identificador:
                    System.out.println("Linea " + linenum + ": " + palabra + " es un IDENTIFICADOR");
                    break;

                case Tipo_dato:
                    System.out.println("Linea " + linenum + ": " + palabra + " es un TIPO DE DATO");
                    break;

                case Numero:
                    System.out.println("Linea " + linenum + ": " + palabra + " es un NUMERO");
                    break;

                case Operador:
                    System.out.println("Linea " + linenum + ": " + palabra + " es un OPERADOR");
                    break;

                case OperadorAritmetico:
                    System.out.println("Linea " + linenum + ": " + palabra + " es un OPERADOR ARITMETICO");
                    break;

                case Asignacion:
                    System.out.println("Linea " + linenum + ": " + palabra + " es una ASIGNACION");
                    break;
            }
        } else {
            System.out.println("Linea " + linenum + ": " + palabra + " no esta detectada");
        }

    }
}
