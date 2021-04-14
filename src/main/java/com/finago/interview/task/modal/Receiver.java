package com.finago.interview.task.modal;

public class Receiver {
    private Integer id;
    private String firstname;
    private String lastname;
    private String file;
    private String hash;

    @SuppressWarnings("unused")
    public Receiver() {
    }

    public Receiver(Integer id, String firstname, String lastname, String file, String hash) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.file = file;
        this.hash = hash;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
