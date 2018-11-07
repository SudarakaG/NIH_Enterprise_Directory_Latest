package com.nih.nih.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "NIH_Directory")
public class NIHNewModel implements Serializable {

    @Id
    @Column(name = "directory_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @Column(name = "directory_legal_name")
    String legal_name;

    @Column(name = "directory_preferred_name")
    String preffered_name;

    @Column(name = "directory_e_mail")
    String email;

    @Column(name = "directory_location")
    String location;

    @Column(name = "directory_mail_stop")
    String mail_stop;

    @Column(name = "directory_phone")
    String phone;

    @Column(name = "directory_fax")
    String fax;

    @Column(name = "directory_ic")
    String ic;

    @Column(name = "directory_organization")
    String organization;

    @Column(name = "directory_classification")
    String classification;

    @Column(name = "directory_tty")
    String tty;

    public NIHNewModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLegal_name() {
        return legal_name;
    }

    public void setLegal_name(String legal_name) {
        this.legal_name = legal_name;
    }

    public String getPreffered_name() {
        return preffered_name;
    }

    public void setPreffered_name(String preffered_name) {
        this.preffered_name = preffered_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMail_stop() {
        return mail_stop;
    }

    public void setMail_stop(String mail_stop) {
        this.mail_stop = mail_stop;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

    @Override
    public String toString() {
        return "NIHNewModel{" +
                "id=" + id +
                ", legal_name='" + legal_name + '\'' +
                ", preffered_name='" + preffered_name + '\'' +
                ", email='" + email + '\'' +
                ", location='" + location + '\'' +
                ", mail_stop='" + mail_stop + '\'' +
                ", phone='" + phone + '\'' +
                ", fax='" + fax + '\'' +
                ", ic='" + ic + '\'' +
                ", organization='" + organization + '\'' +
                ", classification='" + classification + '\'' +
                ", tty='" + tty + '\'' +
                '}';
    }
}