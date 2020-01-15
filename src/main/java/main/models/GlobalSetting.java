package main.models;


import javax.persistence.*;

@Entity
public class GlobalSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String code;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false)
    private String value;

    //========================================================================================


    public GlobalSetting(int id, String code, String name, String value) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.value = value;
    }

    //========================================================================================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
