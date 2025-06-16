package me.rtx4090.developer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public class Developers {
    JDA jda;
    User owner;
    User[] developers;
    User[] acceptNotification;

    public Developers(JDA jda) {
        this.jda = jda;
        owner = jda.getUserById("714032038008848455");
        developers = new User[]{
                jda.getUserById("1113319860760494161"), //ipig
                jda.getUserById("627889024220004362") //redth
        };
        acceptNotification = new User[]{

        };
    }

    public User getOwner() {
        return owner;
    }

    public User[] getDevelopers() {
        return developers;
    }
    public User[] getAcceptNotification() {
        return acceptNotification;
    }
}
