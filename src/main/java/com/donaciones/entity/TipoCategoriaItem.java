package com.donaciones.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "tipo_categoria_items")
public class TipoCategoriaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_categoria_item")
    private Long idTipoCategoriaItem;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TipoCategoriaItem categoriaPadre;

    @Builder.Default
    @OneToMany(mappedBy = "categoriaPadre", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TipoCategoriaItem> subcategorias = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tipoCategoriaItem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemDonacion> items = new ArrayList<>();

    public void addSubcategoria(TipoCategoriaItem subcategoria) {
        subcategorias.add(subcategoria);
        subcategoria.setCategoriaPadre(this);
    }

    public void removeSubcategoria(TipoCategoriaItem subcategoria) {
        subcategorias.remove(subcategoria);
        subcategoria.setCategoriaPadre(null);
    }

}