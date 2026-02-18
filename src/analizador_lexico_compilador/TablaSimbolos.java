package analizador_lexico_compilador;

/**
 *
 * @author fargu
 */
public class TablaSimbolos {

    //TIPOS DE TOKENS
    public enum tokentype {
        Reservada("(module|sub|dim|as|if|then|elseif|else|function|return|while|end)"),
        Identificador ("[A-Za-z][A-Za-z0-9_]*"), //Token válido si inicia con letra y es precedido por letras, nums o _
        Tipo_dato ("(integer|string|boolean|byte)"),
        Numero ("[0-9]+(\\.[0-9]+)?"), //Num ent y decimales
        Operador ("(<|>|<=|>=|<>)"),
        OperadorAritmetico ("(\\+|\\-|\\*|/)"),
        Asignacion ("=");

        public final String type;

        tokentype(String t) {
            this.type = t;
        }
    }
    
    
    //CLASIFICACION DE LOS TOKENS SEGUN SU TIPO
    public tokentype Clasificar (String Token){
    
        //Verificar que el token no venga null
        if (Token == null || Token.isEmpty()){
            return null;
        }
        
       //Normalizar los tokens
       //Trim para eliminar espacios adelante y atrás
       String tokenNormalizado = Token.toLowerCase().trim();
       
       //Verificación de coincidencias
       if (tokenNormalizado.matches(tokentype.Reservada.type)){
           return tokentype.Reservada;
       }
       
       if (tokenNormalizado.matches(tokentype.Tipo_dato.type)){
           return tokentype.Tipo_dato;
       }
       
       if (tokenNormalizado.matches(tokentype.Identificador.type)){
           return tokentype.Identificador;
       }
       
       if (tokenNormalizado.matches(tokentype.Numero.type)){
           return tokentype.Numero;
       }
       
       if (tokenNormalizado.matches(tokentype.Operador.type)){
           return tokentype.Operador;
       }
       
       if (tokenNormalizado.matches(tokentype.OperadorAritmetico.type)){
           return tokentype.OperadorAritmetico;
       }
       
       if (tokenNormalizado.matches(tokentype.Asignacion.type)){
           return tokentype.Asignacion;
       }
       
       //TEMPORAL ***
       return null;
    
    }

}
