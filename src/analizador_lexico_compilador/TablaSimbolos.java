package analizador_lexico_compilador;

/**
 *
 * @author fargu
 */
public class TablaSimbolos {

    public enum tokentype {
        Reservada("(module|sub|dim|as|if|then|elseif|else|function|return|while|end)");

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
       String tokenNormalizado = Token.toLowerCase();
       
       //Verificación de coincidencias
       if (tokenNormalizado.matches(tokentype.Reservada.type)){
           return tokentype.Reservada;
       }
       
       //TEMPORAL ***
       return null;
    
    }

}
