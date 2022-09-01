<template>
  <div>
    <el-tree :data="menus"
             show-checkbox
             node-key="catId"
             :default-expanded-keys="expandedKeys"
             :expand-on-click-node="false"
             draggable
             :allow-drop="allowDrop"
             @node-drop="handleDrop"
             :props="defaultProps">
    <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)">
            Append
          </el-button>
          <el-button type="text" size="mini" @click="edit(data)">edit</el-button>
          <el-button
            v-if="node.childNodes.length === 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)">
            Delete
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="30%"
      :before-close="handleClose">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitData">确 定</el-button>
    </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'category',
  data () {
    return {
      menus: [],
      // 展开节点
      expandedKeys: [],
      dialogVisible: false,
      // 弹窗类型
      dialogType: '', // add,edit
      // 弹窗标题
      title: '',
      maxLevel: 0,
      // 要更新的节点列表
      updateNodes: [],
      category: {
        catId: '',
        name: '',
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        icon: '',
        productUnit: '',
        productCount: null
      },
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  created () {
    this.getMenus()
  },
  methods: {
    getMenus () {
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get'
      }).then(({data}) => {
        this.menus = data
      })
    },
    append (data) {
      console.log('append', data)
      this.dialogType = 'add'
      this.title = '添加分类'
      this.dialogVisible = true
      this.category.parentCid = data.catId
      this.category.catLevel = data.catLevel * 1 + 1
    },
    edit (data) {
      console.log('要修改的数据' + data)
      this.dialogType = 'edit'
      this.title = '修改分类'
      this.dialogVisible = true

      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: 'get'
      }).then(({data}) => {
        // 请求成功
        console.log('要回显的数据', data)
        this.category.name = data.data.name
        this.category.catId = data.data.catId
        this.category.icon = data.data.icon
        this.category.productUnit = data.data.productUnit
        this.category.parentCid = data.data.parentCid
        this.category.catLevel = data.data.catLevel
        this.category.sort = data.data.sort
        this.category.showStatus = data.data.showStatus
      })
    },
    remove (node, data) {
      console.log('remove', node, data)
      var ids = [data.catId]
      this.$confirm(`是否删除【${data.name}】菜单`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'post',
          data: this.$http.adornData(ids, false)
        }).then(({data}) => {
          this.$message({
            message: '菜单删除成功',
            type: 'success'
          })
          // 刷新菜单
          this.getMenus()
          this.expandedKeys = [node.parent.data.catId]
        })
      }).catch(() => {
      })
    },
    handleClose () {
      this.dialogVisible = false
    },
    submitData () {
      if (this.dialogType === 'add') {
        this.addCategory()
      }
      if (this.dialogType === 'edit') {
        this.editCategory()
      }
    },
    addCategory () {
      console.log('提交的三级分类数据', this.category)
      this.$http({
        url: this.$http.adornUrl('/product/category/save'),
        method: 'post',
        data: this.$http.adornData(this.category, false)
      }).then(({data}) => {
        this.$message({
          message: '菜单保存成功',
          type: 'success'
        })
        this.getMenus()
        this.expandedKeys = [this.category.parentCid]
        this.dialogVisible = false
      })
    },
    editCategory () {
      let {catId, name, icon, productUnit} = this.category
      this.$http({
        url: this.$http.adornUrl('/product/category/update'),
        method: 'post',
        data: this.$http.adornData({catId, name, icon, productUnit}, false)
      }).then(({data}) => {
        this.$message({
          message: '菜单修改成功',
          type: 'success'
        })
        this.getMenus()
        this.expandedKeys = [this.category.parentCid]
        this.dialogVisible = false
      })
    },
    allowDrop (draggingNode, dropNode, type) {
      console.log('allowDrop:', draggingNode, dropNode, type)

      // 1、被拖动的当前节点以及所在的父节点总层数不能大于3
      // 1）、被拖动的当前节点总层数
      this.countNodeLevel(draggingNode)
      // 当前正在拖动的节点+父节点所在的深度不大于3即可
      let deep = Math.abs(this.maxLevel - draggingNode.level) + 1
      console.log('深度：', deep)

      //   this.maxLevel
      if (type === 'inner') {
        // console.log(
        //   `this.maxLevel：${this.maxLevel}；draggingNode.data.catLevel：${draggingNode.data.catLevel}；dropNode.level：${dropNode.level}`
        // );
        return deep + dropNode.level <= 3
      } else {
        return deep + dropNode.parent.level <= 3
      }
    },
    countNodeLevel (node) {
      if (node.childNodes && node.childNodes.length !== 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level
          }
          this.countNodeLevel(node.childNodes[i])
        }
      }
    },
    handleDrop: function (draggingNode, dropNode, dropType, ev) {
      // 1、当前节点最新的父节点id
      let pCid
      let siblings
      if (dropType === 'before' || dropType === 'after') {
        pCid = dropNode.parent.data.catId === undefined ? 0 : dropNode.parent.data.catId
        siblings = dropNode.parent.childNodes
      } else {
        pCid = dropNode.data.catId
        siblings = dropNode.childNodes
      }

      // 2、当前拖拽节点的最新顺序
      for (let i = 0; i < siblings.length(); i++) {
        if (siblings[i].data.catId === draggingNode.data.catId) {
          // 如果遍历的是当前正在拖拽的节点
          let catLevel = draggingNode.level
          if (siblings[i].level !== catLevel) {
            // 当前节点层级发生变化
            catLevel = siblings[i].level
            // 设置当前节点子节点层级
            this.updateChildNodeLevel(siblings[i])
          }
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pCid,
            catLevel: catLevel
          })
        } else {
          this.updateNodes.push({catId: siblings[i].data.catId, sort: i})
        }
      }

      // 3、当前拖拽节点的最新层级
      console.log('updateNodes', this.updateNodes)
    },
    updateChildNodeLevel (node) {
      if (node.childNodes && node.childNodes.length !== 0) {
        for (let i = 0; i < this.childNodes.length(); i++) {
          this.updateNodes.push({catId: this.childNodes[i].data.catId, catLevel: this.childNodes[i].level})
          this.updateChildNodeLevel(node.childNodes[i])
        }
      }
    }
  }
}
</script>

<style scoped>

</style>
