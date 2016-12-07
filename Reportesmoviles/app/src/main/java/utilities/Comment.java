package utilities;

import java.io.Serializable;

/**
 * Created by Tiago on 5/12/16.
 */
public class Comment implements Serializable{
    private String idUsuario;
    private String comentario;

    public Comment(String idUsuario, String comentario) {
        this.idUsuario = idUsuario;
        this.comentario = comentario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
