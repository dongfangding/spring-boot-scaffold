package com.ddf.scaffold.fw.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.lang.reflect.Field;

/**
 * @author dongfang.ding on 2018/12/20
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldExtend {
	private Field field;
	private Column column;
}
