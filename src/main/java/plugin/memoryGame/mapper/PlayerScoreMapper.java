package plugin.memoryGame.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import plugin.memoryGame.mapper.data.PlayerScore;


public interface PlayerScoreMapper {

  @Select("select * from player_score")
  List<PlayerScore> selectList();

  @Insert("insert player_score(player_name, score, difficulty, clearTime, registered_at) values(#{playerName}, #{score}, #{difficulty}, #{clearTime}, now())")
  int insert(PlayerScore playerScore);

}