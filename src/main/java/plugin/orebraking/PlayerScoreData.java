package plugin.orebraking;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import plugin.orebraking.mapper.PlayerScoreMapper;
import plugin.orebraking.mapper.data.PlayerScore;

import java.io.InputStream;
import java.util.List;

/**
 * DB接続やそれに付随する登録や更新処理を行うクラスです。
 */
public class PlayerScoreData {

    private final SqlSessionFactory sqlSessionFactory;
    private final PlayerScoreMapper mapper;

    public PlayerScoreData() {
        try {
            InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession session = sqlSessionFactory.openSession(true);
            this.mapper = session.getMapper(PlayerScoreMapper.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * オーアブレイクスコアテーブルから一覧でスコア情報を取得する。
     *
     * @return スコア情報をの一覧
     */

    public List<PlayerScore> selectList() {
            return mapper.selectList();
    }

    /**
     * オーアブレイクプレイヤースコアテーブルにスコア情報を登録する。
     *
     * @param playerScore プレイヤースコア
     */
    public void insert(PlayerScore playerScore) {
            mapper.insert(playerScore);
        }
}

