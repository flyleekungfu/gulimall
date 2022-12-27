<template>
  <el-tree :data="menus"
           node-key="catId"
           :default-expanded-keys="expandedKeys"
           :expand-on-click-node="false"
           ref="menuTree"
           @node-click="nodeClick"
           :props="defaultProps">
  </el-tree>
</template>

<script>
export default {
  name: 'category',
  data() {
    return {
      menus: [],
      // 展开节点
      expandedKeys: [],
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
    nodeClick(data, node, component) {
      console.log('子组件category的节点被点击', data, node, component)
      // 向父组件发送事件
      this.$emit('tree-node-click', data, node, component)
    }
  }
}
</script>

<style scoped>

</style>
