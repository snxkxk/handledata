package tk.mybatis;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface CommonTkMapper<T> extends Mapper<T>, MySqlMapper {

}
