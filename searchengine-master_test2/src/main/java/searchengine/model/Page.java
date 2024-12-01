package searchengine.model;

import javax.persistence.*;

@Entity
@Table(name = "page", indexes = {@javax.persistence.Index(name = "idx_page_path", columnList = "path")})
public class Page{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "site_id")
    private int siteId;
    @Column(name= "path", nullable = false)
    private String path;
    @Column(name= "code", nullable = false)
    private int code;
    @Column(name= "content",columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false, insertable = false, updatable = false)
    private Site site;

//    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<searchengine.model.Index> indices = new ArrayList<>();






    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }
}
