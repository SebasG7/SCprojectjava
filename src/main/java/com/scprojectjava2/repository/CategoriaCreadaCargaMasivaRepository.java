package com.scprojectjava2.repository;

import com.scprojectjava2.model.CategoriaCreadaCargaMasiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaCreadaCargaMasivaRepository extends JpaRepository<CategoriaCreadaCargaMasiva, Integer> {

    List<CategoriaCreadaCargaMasiva> findByOperacionCargaIdAndRevertidaFalse(Integer operacionCargaId);

    @Query("SELECT c FROM CategoriaCreadaCargaMasiva c WHERE c.operacionCarga.id = :operacionId AND c.revertida = false")
    List<CategoriaCreadaCargaMasiva> findCategoriasNoRevertidasPorOperacion(@Param("operacionId") Integer operacionId);

    List<CategoriaCreadaCargaMasiva> findByOperacionCargaId(Integer operacionCargaId);
}
