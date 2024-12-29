package searchengine.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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

    @OneToMany(mappedBy = "siteEntity", orphanRemoval = true)
    @ToString.Exclude
    private List<Lemma> lemmas = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "siteEntity", orphanRemoval = true)
    private List<Page> pages = new ArrayList<>();

}
