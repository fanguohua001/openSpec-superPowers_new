-- 跳棋菜单增量脚本
-- 用途：对已经初始化过的 RuoYi 数据库补充“休闲游戏 / 跳棋”菜单。

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
) values (
  123, '休闲游戏', 0, 6, 'game', null, '', '',
  1, 0, 'M', '0', '0', '', 'bug',
  'admin', sysdate(), '', null, '休闲游戏目录'
) on duplicate key update
  menu_name = values(menu_name),
  parent_id = values(parent_id),
  order_num = values(order_num),
  path = values(path),
  component = values(component),
  `query` = values(`query`),
  route_name = values(route_name),
  is_frame = values(is_frame),
  is_cache = values(is_cache),
  menu_type = values(menu_type),
  visible = values(visible),
  status = values(status),
  perms = values(perms),
  icon = values(icon),
  update_by = 'admin',
  update_time = sysdate(),
  remark = values(remark);

insert into sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
) values (
  125, '跳棋', 123, 2, 'chinese-checkers', 'game/chinese-checkers/index', '', '',
  1, 0, 'C', '0', '0', 'game:chinese-checkers:view', 'guide',
  'admin', sysdate(), '', null, '中国跳棋游戏菜单'
) on duplicate key update
  menu_name = values(menu_name),
  parent_id = values(parent_id),
  order_num = values(order_num),
  path = values(path),
  component = values(component),
  `query` = values(`query`),
  route_name = values(route_name),
  is_frame = values(is_frame),
  is_cache = values(is_cache),
  menu_type = values(menu_type),
  visible = values(visible),
  status = values(status),
  perms = values(perms),
  icon = values(icon),
  update_by = 'admin',
  update_time = sysdate(),
  remark = values(remark);

insert ignore into sys_role_menu (role_id, menu_id)
select role_id, 123 from sys_role where role_key = 'common';

insert ignore into sys_role_menu (role_id, menu_id)
select role_id, 125 from sys_role where role_key = 'common';
