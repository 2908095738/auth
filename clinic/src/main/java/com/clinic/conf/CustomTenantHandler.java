package com.clinic.conf;

import net.sf.jsqlparser.schema.Column;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.clinic.util.LoginUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class CustomTenantHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        // 假设有一个租户上下文，能够从中获取当前用户的租户
        Long userId = LoginUser.getId();
        // 返回租户ID的表达式，LongValue 是 JSQLParser 中表示 bigint 类型的 class
        return new LongValue(userId);
    }

    @Override
    public String getTenantIdColumn() {
        return "user_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        //如果那些表不需要拼接多租户条件,
        List<String> tableList = new ArrayList<>();
        tableList.add("unit");
        tableList.add("auxiliary_type");
        tableList.add("drug");
        tableList.add("drug_order_detail");
        tableList.add("drug_tag");
        tableList.add("prescription_drug");
        tableList.add("stock_unit");
        tableList.add("usage");
        tableList.add("dossier_prescription");
        tableList.add("retail_drug_record");

        //使用了缓存的表，不需要拼接租户条件
        tableList.add("setting");
        tableList.add("inform");
        if(tableList.contains(tableName)){
            //如果不需要添加的表名称在list中,就返回true,不用拼接租户条件
            return true;
        }
        return false;
    }

    /**
     * 获取租户 ID 字段名
     */
    @Override
    public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
        return com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler.super.ignoreInsert(columns, tenantIdColumn);
    }


}