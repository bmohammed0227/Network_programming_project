import java.time.LocalDateTime;
import java.util.Date;

public class Compte {
    private String pseudo;
    private String password;
    private String email;
    private String firstname;
    private String familyname;
    private boolean status;
    private Date date;

    public Compte(String pseudo, String password, String email, boolean status) {
        super();
        this.pseudo = pseudo;
        this.password = password;
        this.email = email;
        this.status = status;
        this.date = new Date();
    }
    public Compte(String pseudo, String email, String firstname, String familyname, String password, boolean status) {
        super();
        this.pseudo = pseudo;
        this.password = password;
        this.email = email;
        this.status = status;
        this.familyname = familyname;
        this.firstname = firstname;
        this.date = new Date();
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String toString() {
        return "pseudo : " + this.getPseudo() + ", password : " + this.getPassword() + ", email : " + this.getEmail()
                + ", status : " + this.isStatus() + "\n"+", Nom : "+this.getFamilyname()+", Prénom : "+this.getFirstname();
    }

    public Date getDate() {
        return date;
    }

    public void setDate() {
        this.date = new Date();
        if (!status)
            status = true;
    }

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getFamilyname() {
		return familyname;
	}

	public void setFamilyname(String familyname) {
		this.familyname = familyname;
	}

}
