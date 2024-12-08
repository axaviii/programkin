package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    @Column(name= "lemma", columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(name = "frequency",nullable = false)
    private int frequency;




}
