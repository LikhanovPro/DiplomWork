package main.requestObject;

public class GeneralPostMProfileObject {

    boolean removePhoto;
    String name;
    String email;
    String password;


    public boolean isRemovePhoto() {
        return removePhoto;
    }

    public String getName() {
        return name;
    }

    public String geteMail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setRemovePhoto(boolean removePhoto) {
        this.removePhoto = removePhoto;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public GeneralPostMProfileObject(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
