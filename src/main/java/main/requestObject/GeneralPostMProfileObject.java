package main.requestObject;

public class GeneralPostMProfileObject {

    String photo;
    boolean removePhoto;
    String name;
    String eMail;
    String password;

    public String getPhoto() {
        return photo;
    }

    public boolean isRemovePhoto() {
        return removePhoto;
    }

    public String getName() {
        return name;
    }

    public String geteMail() {
        return eMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setRemovePhoto(boolean removePhoto) {
        this.removePhoto = removePhoto;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
