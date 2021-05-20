package com.sabiha.cardmaker.firebase_model;

public class MyCards {
    private String cardtemp;
    private String id;

    private String editedcard;
    private String designation;
    private String email;
    private String mobile;
    private String name;
    private String skill1;
    private String skill2;
    private String skill3;

    public MyCards() {//To remove error of Class com.sabiha.cardmaker.firebase_model.MyCards does not define a no-argument constructor. If you are using ProGuard, make sure these constructors are not stripped.
    }

    public MyCards(String cardtemp, String editedcard, String id, String designation, String email, String mobile, String name, String skill1, String skill2, String skill3) {
        this.cardtemp = cardtemp;
        this.id = id;
        this.designation = designation;
        this.email = email;
        this.mobile = mobile;
        this.name = name;
        this.skill1 = skill1;
        this.skill2 = skill2;
        this.skill3 = skill3;
        this.editedcard = editedcard;

    }

    public String getEditedcard() {
        return editedcard;
    }

    public void setEditedcard(String editedcard) {
        this.editedcard = editedcard;
    }

    public String getCardtemp() {
        return cardtemp;
    }

    public void setCardtemp(String cardtemp) {
        this.cardtemp = cardtemp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkill1() {
        return skill1;
    }

    public void setSkill1(String skill1) {
        this.skill1 = skill1;
    }

    public String getSkill2() {
        return skill2;
    }

    public void setSkill2(String skill2) {
        this.skill2 = skill2;
    }

    public String getSkill3() {
        return skill3;
    }

    public void setSkill3(String skill3) {
        this.skill3 = skill3;
    }

}
