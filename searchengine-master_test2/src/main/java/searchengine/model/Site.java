package searchengine.model;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
    @Column(name= "status")
    private Status status;
    @Column(name= "status_time")
    private Date statusTime;
    @Column(name= "last_error_text", columnDefinition = "TEXT")
    private String lastErrorText;
    @Column(name= "url", columnDefinition = "VARCHAR(255)")
    private String url;
    @Column(name= "name", columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lemma> lemmas = new ArrayList<>();
    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Page> pages = new ArrayList<>();



    public List<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(List<Lemma> lemmas) {
        this.lemmas = lemmas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastErrorText() {
        return lastErrorText;
    }

    public void setLastErrorText(String lastErrorText) {
        this.lastErrorText = lastErrorText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return id == site.id && status == site.status && Objects.equals(statusTime, site.statusTime) && Objects.equals(lastErrorText, site.lastErrorText) && Objects.equals(url, site.url) && Objects.equals(name, site.name) && Objects.equals(lemmas, site.lemmas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, statusTime, lastErrorText, url, name, lemmas);
    }
}
