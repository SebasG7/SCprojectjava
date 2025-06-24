package com.scprojectjava2.repository;

import com.scprojectjava2.model.UnidadCreadaCargaMasiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadCreadaCargaMasivaRepository extends JpaRepository<UnidadCreadaCargaMasiva, Integer> {

    List<UnidadCreadaCargaMasiva> findByOperacionCargaIdAndRevertidaFalse(Integer operacionCargaId);

    @Query("SELECT u FROM UnidadCreadaCargaMasiva u WHERE u.operacionCarga.id = :operacionId AND u.revertida = false")
    List<UnidadCreadaCargaMasiva> findUnidadesNoRevertidasPorOperacion(@Param("operacionId") Integer operacionId);

    List<UnidadCreadaCargaMasiva> findByOperacionCargaId(Integer operacionCargaId);
}
