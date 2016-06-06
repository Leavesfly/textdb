package io.leavesfly.textdb.bean;


public class T_FamilyMemberBean
{
    private String id;
    private String name;
    private String gender;
    private String mobile;
    private String email;
    private String address;
    private String family_id;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getFamily_id()
    {
        return family_id;
    }

    public void setFamily_id(String family_id)
    {
        this.family_id = family_id;
    }
}
