package analizador_lexico_compilador;

/**
 *
 * @author fargu
 */
public class TablaSimbolos {

    public enum tokentype {
        Reservada("(module|sub|dim|as|if|then|elseif|else|function|return|while|end)"),
        Identificador ("[A-Za-z][A-Za-z0-9_]*"); //Token válido si inicia con letra y es precedido por letras, nums o _

        public final String type;

        tokentype(String t) {
            this.type = t;
        }
    }
    
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
       
       if (tokenNormalizado.matches(tokentype.Identificador.type)){
           return tokentype.Identificador;
       }
       
       //TEMPORAL ***
       return null;
    
    }

}
