export type TableListItem = {
<#list fieldList as obj>
  ${obj.field_name}: string;
</#list>
};

export type TableListPagination = {
  total: number;
  pageSize: number;
  current: number;
};

export type TableListData = {
  list: TableListItem[];
  pagination: Partial<TableListPagination>;
};

export type TableListParams = {
  <#list fieldList as obj>
    ${obj.field_name}?: string;
  </#list>
  id?: string;
  pageSize?: number;
  currentPage?: number;
  filter?: Record<string, any[]>;
  sorter?: Record<string, any>;
};