package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "page", indexes = {@javax.persistence.Index(name = "idx_page_path", columnList = "path")})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Page{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name= "path", nullable = false)
    private String path;
    @Column(name= "code", nullable = false)
    private int code;
    @Column(name= "content",columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

//    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<searchengine.model.Index> indices = new ArrayList<>();


}
