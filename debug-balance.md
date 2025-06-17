# 财务金额表余额计算问题排查指南

## 问题描述
充值100元，但余额显示200元

## 排查步骤

### 1. 检查数据库记录

#### 查看财务金额表记录
```sql
SELECT * FROM erp_finance_amount 
WHERE creator = '您的用户名' 
AND channel_type = '微信' 
ORDER BY create_time DESC;
```

#### 查看财务记录表
```sql
SELECT * FROM erp_finance 
WHERE creator = '您的用户名' 
AND account = '微信' 
ORDER BY create_time DESC;
```

### 2. 检查计算逻辑

#### 余额计算公式
```
当前余额 = 财务金额表最新记录的after_balance + 财务表收支影响
```

#### 累计充值计算公式
```
累计充值 = 财务金额表中所有operation_type=1的记录的amount之和
```

### 3. 可能的问题原因

#### 原因1：重复充值记录
- 检查财务金额表是否有多条充值记录
- 每次充值都会创建新记录，累计充值是所有记录的总和

#### 原因2：财务表影响计算错误
- 检查财务表中是否有相同渠道的收支记录
- 如果有，会被加到余额计算中

#### 原因3：前端显示错误
- 检查前端是否正确解析后端返回的数据
- 检查formatFinanceAmount函数是否有问题

### 4. 修复建议

#### 如果是重复记录问题
```java
// 确保充值时不会在财务表中创建记录
// updateBalance方法应该为空实现
```

#### 如果是计算逻辑问题
```java
// 确保getChannelBalance只计算一次
// 避免递归调用
```

#### 如果是前端显示问题
```javascript
// 检查balanceSummary的数据结构
console.log('余额汇总数据:', balanceSummary.value)
```

## 调试方法

1. **后端调试**：启动服务后查看控制台调试日志
2. **前端调试**：在浏览器控制台查看API返回数据
3. **数据库调试**：直接查询数据库表记录

## 预期结果

- 充值100元，财务金额表应该有1条记录：amount=100, after_balance=100
- 余额显示应该是100元
- 累计充值显示应该是100元（如果是首次充值） 