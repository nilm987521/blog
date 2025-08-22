return {
  -- Jenkinsfile 语法高亮
  {
    "nvim-treesitter/nvim-treesitter",
    opts = function(_, opts)
      if type(opts.ensure_installed) == "table" then
        vim.list_extend(opts.ensure_installed, { "groovy" })
      end
    end,
  },

  -- Jenkinsfile 文件类型检测
  {
    "neovim/nvim-lspconfig",
    opts = {
      servers = {
        groovyls = {
          -- Groovy Language Server for Jenkinsfile
          filetypes = { "groovy", "Jenkinsfile" },
        },
      },
    },
  },

  -- 文件类型检测
  {
    "nathom/filetype.nvim",
    config = function()
      require("filetype").setup({
        overrides = {
          literal = {
            ["Jenkinsfile"] = "groovy",
          },
          pattern = {
            [".*Jenkinsfile.*"] = "groovy",
            [".*\\.jenkinsfile"] = "groovy",
            [".*\\.Jenkinsfile"] = "groovy",
          },
        },
      })
    end,
  },

  -- Jenkinsfile 代码补全
  {
    "hrsh7th/nvim-cmp",
    dependencies = {
      -- 添加 Jenkinsfile 特定的补全源
      {
        "L3MON4D3/LuaSnip",
        config = function()
          local ls = require("luasnip")
          local s = ls.snippet
          local t = ls.text_node
          local i = ls.insert_node

          -- Jenkinsfile 代码片段
          ls.add_snippets("groovy", {
            s("pipeline", {
              t("pipeline {"),
              t({"", "    agent any"}),
              t({"", "    stages {"}),
              t({"", "        stage('"), i(1, "Build"), t("') {"),
              t({"", "            steps {"}),
              t({"", "                "), i(2, "// Add your build steps here"),
              t({"", "            }"}),
              t({"", "        }"}),
              t({"", "    }"}),
              t({"", "}"}),
            }),
            s("stage", {
              t("stage('"), i(1, "Stage Name"), t("') {"),
              t({"", "    steps {"}),
              t({"", "        "), i(2, "// Add steps here"),
              t({"", "    }"}),
              t({"", "}"}),
            }),
            s("agent", {
              t("agent {"),
              t({"", "    "), i(1, "any"),
              t({"", "}"}),
            }),
            s("post", {
              t("post {"),
              t({"", "    "), i(1, "always"), t(" {"),
              t({"", "        "), i(2, "// Post actions"),
              t({"", "    }"}),
              t({"", "}"}),
            }),
          })
        end,
      },
    },
  },

  -- Jenkins 语法高亮主题
  {
    "folke/tokyonight.nvim",
    opts = {
      on_highlights = function(hl, c)
        hl.groovyKeyword = { fg = c.purple }
        hl.groovyString = { fg = c.green }
        hl.groovyComment = { fg = c.comment }
      end,
    },
  },
}
