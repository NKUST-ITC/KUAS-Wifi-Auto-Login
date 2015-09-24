<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class LoginFrm
    Inherits System.Windows.Forms.Form

    'Form 覆寫 Dispose 以清除元件清單。
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    '為 Windows Form 設計工具的必要項
    Private components As System.ComponentModel.IContainer

    '注意: 以下為 Windows Form 設計工具所需的程序
    '可以使用 Windows Form 設計工具進行修改。
    '請勿使用程式碼編輯器進行修改。
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Dim resources As System.ComponentModel.ComponentResourceManager = New System.ComponentModel.ComponentResourceManager(GetType(LoginFrm))
        Me.Pwd = New System.Windows.Forms.TextBox()
        Me.User = New System.Windows.Forms.TextBox()
        Me.LogoutButton = New System.Windows.Forms.Button()
        Me.LoginButton = New System.Windows.Forms.Button()
        Me.SuspendLayout()
        '
        'Pwd
        '
        Me.Pwd.Location = New System.Drawing.Point(12, 44)
        Me.Pwd.Margin = New System.Windows.Forms.Padding(3, 4, 3, 4)
        Me.Pwd.Name = "Pwd"
        Me.Pwd.PasswordChar = Global.Microsoft.VisualBasic.ChrW(9679)
        Me.Pwd.Size = New System.Drawing.Size(276, 23)
        Me.Pwd.TabIndex = 7
        '
        'User
        '
        Me.User.AccessibleDescription = ""
        Me.User.Location = New System.Drawing.Point(12, 13)
        Me.User.Margin = New System.Windows.Forms.Padding(3, 4, 3, 4)
        Me.User.Name = "User"
        Me.User.Size = New System.Drawing.Size(276, 23)
        Me.User.TabIndex = 6
        '
        'LogoutButton
        '
        Me.LogoutButton.Font = New System.Drawing.Font("微軟正黑體", 9.75!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(136, Byte))
        Me.LogoutButton.Location = New System.Drawing.Point(153, 76)
        Me.LogoutButton.Margin = New System.Windows.Forms.Padding(3, 5, 3, 5)
        Me.LogoutButton.Name = "LogoutButton"
        Me.LogoutButton.Size = New System.Drawing.Size(135, 40)
        Me.LogoutButton.TabIndex = 8
        Me.LogoutButton.Text = "登出"
        Me.LogoutButton.UseVisualStyleBackColor = True
        '
        'LoginButton
        '
        Me.LoginButton.Font = New System.Drawing.Font("微軟正黑體", 9.75!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(136, Byte))
        Me.LoginButton.Location = New System.Drawing.Point(12, 76)
        Me.LoginButton.Margin = New System.Windows.Forms.Padding(3, 5, 3, 5)
        Me.LoginButton.Name = "LoginButton"
        Me.LoginButton.Size = New System.Drawing.Size(135, 40)
        Me.LoginButton.TabIndex = 9
        Me.LoginButton.Text = "登入"
        Me.LoginButton.UseVisualStyleBackColor = True
        '
        'LoginFrm
        '
        Me.AcceptButton = Me.LoginButton
        Me.AutoScaleDimensions = New System.Drawing.SizeF(7.0!, 16.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(300, 130)
        Me.Controls.Add(Me.LoginButton)
        Me.Controls.Add(Me.LogoutButton)
        Me.Controls.Add(Me.Pwd)
        Me.Controls.Add(Me.User)
        Me.Font = New System.Drawing.Font("微軟正黑體", 9.0!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(136, Byte))
        Me.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle
        Me.Icon = CType(resources.GetObject("$this.Icon"), System.Drawing.Icon)
        Me.Margin = New System.Windows.Forms.Padding(3, 4, 3, 4)
        Me.MaximizeBox = False
        Me.MinimizeBox = False
        Me.Name = "LoginFrm"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "高應無線通"
        Me.ResumeLayout(False)
        Me.PerformLayout()

    End Sub

    Friend WithEvents Pwd As TextBox
    Friend WithEvents User As TextBox
    Friend WithEvents LogoutButton As Button
    Friend WithEvents LoginButton As Button
End Class
