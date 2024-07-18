package Client.services;

import java.io.Serializable;
public class Skill implements Serializable{
private static final long serialVersionUID = 1L;
    private int id;
    private String skill;
    private String experience;

    public Skill(int id, String skill, String experience) {
        this.id = id;
        this.skill = skill;
        this.experience = experience;
    }

    public Skill() {
        this.id = 0;
        this.skill = "";
        this.experience = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }
}
