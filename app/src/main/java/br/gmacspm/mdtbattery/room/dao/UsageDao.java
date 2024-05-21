package br.gmacspm.mdtbattery.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

import br.gmacspm.mdtbattery.models.UsageModel;

@Dao
public interface UsageDao {
    // Inserir novo tempo no Banco De Dados.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDischargeHistory(UsageModel usageModel);

    // Query que retorna todos os nossos tempos em uma List.
    @Query("SELECT * FROM usage_database ORDER BY level DESC")
    List<UsageModel> getDischargeHistory();

    // Query que será responsável por deletar todos os tempos do banco de dados: usage_database.
    @Query("DELETE FROM usage_database")
    void deleteAllHistory();
}
