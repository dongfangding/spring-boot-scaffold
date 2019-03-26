package com.ddf.scaffold.fw.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.lang.reflect.Field;

/**
 * @author DDf on 2019/1/24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldExtend {
	private Field field;
	private Column column;
}
