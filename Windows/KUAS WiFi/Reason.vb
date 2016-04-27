Module Reason
    Public Function dumpReason(_reasonCode As Integer) As String
        Select Case _reasonCode
            Case 1
                Return "登入頁面出現了系統錯誤！"
            Case 2
                Return "沒有指定認證策略！"
            Case 3
                Return "認證策略中自動增加指定的組不存在！"
            Case 7
                Return "用戶已經被列入黑名單！"
            Case 8
                Return "超出帳號最大登入數！"
            Case 9
                Return "帳號綁定檢查失敗！"
            Case 10, 44, 56
                Return "帳號不存在！"
            Case 11, 45, 57
                Return "密碼不正確！"
            Case 12
                Return "該帳號已經被凍結！"
            Case 20, 21, 22, 25
                Return "連接RADIUS伺服器時，發生了故障(" + _reasonCode + ")！"
            Case 24
                Return "無法連接到指定的RADIUS伺服器！"
            Case 26
                Return "RADIUS伺服器回應數據不正確！"
            Case 27, 35
                Return "認證失敗，請檢查您的帳號及密碼。"
            Case 30
                Return "無法連接到指定的POP3伺服器！"
            Case 31, 32
                Return "連接POP3伺服器時，發生了故障(" + _reasonCode + ")！"
            Case 33, 34
                Return "POP3伺服器回應數據不正確(" + _reasonCode + ")！"
            Case 40, 42, 43
                Return "連接LDAP伺服器時，發生了故障(" + _reasonCode + ")！"
            Case 41
                Return "無法連接到指定的LDAP伺服器！"
            Case 50
                Return "AD伺服器域名配置錯誤！"
            Case 51, 53, 54
                Return "連接AD伺服器時，發生了故障(" + _reasonCode + ")！"
            Case 52
                Return "無法連接到指定的AD伺服器！"
            Case 55
                Return "AD伺服器的查詢密碼不正確！"
            Case 60
                Return "登入失敗次數超出最大限制！"
            Case Else
                Return "web_auth_error_" & _reasonCode
        End Select
    End Function

End Module
