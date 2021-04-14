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

    public Integer getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }
}
