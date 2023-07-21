package plugin.orebraking.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.bukkit.entity.Player;
import plugin.orebraking.mapper.data.PlayerScore;

import java.util.List;

public interface PlayerScoreMapper {

    @Select("select * from ore_braking_player_score")
    List<PlayerScore> selectList();

    @Insert("insert ore_braking_player_score(player_name, score, registered_at) values (#{playerName}, #{score}, now())")
    int insert(PlayerScore playerScore);
}
