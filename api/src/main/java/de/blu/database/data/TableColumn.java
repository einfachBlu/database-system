package de.blu.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class TableColumn {
  private TableColumnType columnType;
  private String name;
  private boolean isPrimaryKey;
}
