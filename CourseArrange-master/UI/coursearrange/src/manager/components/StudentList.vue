<template>
  <div class="page-container">
    <div class="section-card">
      <div class="header-actions">
        <h2 class="page-title">学生管理</h2>
        <div class="action-buttons">
          <el-button type="primary" icon="el-icon-plus" @click="addStudent">添加学生</el-button>
          <el-button type="success" icon="el-icon-upload2">批量导入</el-button>
        </div>
      </div>

      <!-- 搜索和筛选区域 -->
      <div class="search-section">
        <el-input
          v-model="keyword"
          placeholder="请输入学生姓名"
          clearable
          @clear="inputListener"
          style="width: 200px; margin-right: 10px;">
          <el-button slot="append" icon="el-icon-search" @click="searchStudent">搜索</el-button>
        </el-input>
        
        <el-select
          v-model="value1"
          placeholder="年级"
          clearable
          @change="queryClass"
          @clear="gradeListener"
          style="width: 120px; margin-right: 10px;">
          <el-option
            v-for="item in grade"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        
        <el-select
          v-model="value2"
          placeholder="班级"
          clearable
          @change="queryStudentByClass"
          @clear="classListener"
          style="width: 120px;">
          <el-option
            v-for="item in classNo"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="studentData"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266',
          fontWeight: 'bold',
          fontSize: '14px',
          padding: '12px 0'
        }"
        :cell-style="{
          padding: '8px 0'
        }">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="studentNo" label="学号" width="100"></el-table-column>
        <el-table-column prop="realname" label="姓名" width="90"></el-table-column>
        <el-table-column prop="username" label="昵称" width="90"></el-table-column>
        <el-table-column prop="grade" label="年级" width="80"></el-table-column>
        <el-table-column prop="classNo" label="班级" width="90"></el-table-column>
        <el-table-column prop="age" label="年龄" width="70"></el-table-column>
        <el-table-column prop="telephone" label="电话" width="120"></el-table-column>
        <el-table-column prop="email" label="邮件" width="180"></el-table-column>
        <el-table-column prop="address" label="地址" min-width="200"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="editById(scope.$index, scope.row)">编辑</el-button>
            <el-button type="danger" size="mini" @click="deleteById(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page.sync="page"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog title="编辑学生" :visible.sync="visibleForm" width="500px">
      <el-form
        :model="editFormData"
        label-position="left"
        label-width="80px"
        :rules="editFormRules"
      >
        <el-form-item label="学号">
          <el-input v-model="editFormData.studentNo" autocomplete="off" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="username">
          <el-input v-model="editFormData.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realname">
          <el-input v-model="editFormData.realname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="editFormData.grade" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="班级" prop="classNo">
          <el-input v-model="editFormData.classNo" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="telephone">
          <el-input v-model="editFormData.telephone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮件" prop="email">
          <el-input v-model="editFormData.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editFormData.address" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model="editFormData.age" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="visibleForm = false">取 消</el-button>
        <el-button type="primary" @click="commit()">提 交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "StudentList",
  data() {
    return {
      studentData: [],
      editFormData: [],
      keyword: "",
      page: 1,
      pageSize: 10,
      total: 0,
      value1: "", // 年级
      value2: "", // 班级
      grade: [
        {
          value: "01",
          label: "高一"
        },
        {
          value: "02",
          label: "高二"
        },
        {
          value: "03",
          label: "高三"
        }
      ],
      classNo: [
        {
          value: "",
          label: ""
        }
      ],
      visibleForm: false,
      editFormRules: {
        username: [{ required: true, message: "请输入昵称", trigger: "blur" }],
        realname: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        grade: [{ required: true, message: "请输入年级", trigger: "blur" }],
        classNo: [{ required: true, message: "请输入班级", trigger: "blur" }],
        telephone: [{ required: true, message: "请输入联系电话", trigger: "blur" }],
        email: [{ required: true, message: "请输入邮件", trigger: "blur" }],
        address: [{ required: true, message: "请输入地址", trigger: "blur" }],
        age: [{ required: true, message: "请输入年龄", trigger: "blur" }]
      }
    };
  },

  mounted() {
    this.allStudent();
  },

  methods: {

    // 清空年级回到查询所有学生
    gradeListener() {
      this.allStudent()
      this.value2 = ''
    },

    // 清空班级回到查询所有班级
    classListener() {

    },

    // 查询班级信息
    queryClass() {
      this.$axios
        .get("http://localhost:8080/class-grade/" + this.value1)
        .then(res => {
          let ret = res.data.data
          this.classNo.splice(0, this.classNo.length)
          this.value2 = ""
          ret.map(v => {
            this.classNo.push({
              value: v.classNo,
              label: v.className
            });
          });
        })
        .catch(error => {
          
        });
    },

    // 根据班级查询学生信息
    queryStudentByClass() {
      this.$axios
        .get(
          "http://localhost:8080/student-class/" + this.page + "/" + this.value2
        )
        .then(res => {
          console.log(res)
          if (res.data.code == 0) {
            let ret = res.data.data
            this.studentData = ret.records
            this.total = ret.total
          }
        })
        .catch(error => {

        });
    },

    /***
     * 编辑提交
     */
    commit() {
      this.modifyStudent(this.editFormData)
    },

    inputListener() {
      this.allStudent()
    },

    /**
     * 查询所有学生
     */
    allStudent() {
      this.$axios
        .get("http://localhost:8080/student/students/" + this.page)
        .then(res => {
          let ret = res.data.data
          this.studentData = ret.records
          this.total = ret.total
          // this.$message({message:'查询成功', type: 'success'})
        })
        .catch(error => {
          this.$message.error("查询学生列表失败")
        });
    },

    /**
     * 关键字查询学生
     */
    searchStudent() {
      this.$axios
        .get("http://localhost:8080/student/search/" + this.keyword)
        .then(res => {
          let ret = res.data.data
          this.studentData = ret.records
          this.total = ret.total
          this.$message({ message: "查询成功", type: "success" })
        })
        .catch(error => {
          this.$message.error("查询失败")
        });
    },

    /**
     * 根据id删除学生
     */
    deleteById(index, row) {
      this.deleteStudentById(row.id)
    },

    deleteStudentById(id) {
      this.$axios
        .delete("http://localhost:8080/student/delete/" + id)
        .then(res => {
          this.$message({ message: "删除成功", type: "success" })
          this.allStudent()
        })
        .catch(error => {
          this.$message.error("删除失败")
        });
    },

    /**
     * 编辑学生
     */
    editById(index, row) {
      let modifyId = row.id
      this.editFormData = row
      this.visibleForm = true
    },

    /**
     * 更新学生
     */
    modifyStudent(modifyData) {
      this.$axios
        .post("http://localhost:8080/student/modify/" + this.editFormData.id, modifyData)
        .then(res => {
          this.$message({ message: "更新成功", type: "success" })
          this.allStudent()
          this.visibleForm = false
        })
        .catch(error => {
          this.$message.error("更新失败")
        });
    },

    handleSizeChange() {},

    handleCurrentChange(v) {
      this.page = v
      this.allStudent()
    }
  }
};
</script>

<style lang="less" scoped>
.page-container {
  padding: 20px;
  background-color: #f0f2f5;
}

.section-card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  
  .page-title {
    font-size: 20px;
    color: #303133;
    margin: 0;
    font-weight: 500;
  }

  .action-buttons {
    .el-button {
      margin-left: 10px;
      padding: 9px 15px;
    }
  }
}

.search-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .el-input {
    margin-right: 10px;
  }
}

.el-table {
  margin-bottom: 20px;
  
  ::v-deep .el-table__header th {
    padding: 12px 0;
    background-color: #f5f7fa;
  }
  
  ::v-deep .el-table__body td {
    padding: 8px 0;
  }
  
  ::v-deep .el-button {
    padding: 7px 12px;
    & + .el-button {
      margin-left: 6px;
    }
  }
}

.pagination-container {
  text-align: right;
  padding-top: 20px;
  
  ::v-deep .el-pagination {
    padding: 2px 0;
    font-weight: normal;
    
    .el-pagination__total,
    .el-pagination__sizes,
    .el-pagination__jump {
      margin-right: 10px;
    }
  }
}

::v-deep .el-dialog {
  .el-dialog__header {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
  }
  
  .el-dialog__body {
    padding: 20px;
  }
  
  .el-dialog__footer {
    padding: 15px 20px;
    border-top: 1px solid #ebeef5;
  }
  
  .el-form-item {
    margin-bottom: 20px;
  }
}
</style>