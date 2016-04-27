Imports System.Text
Imports System.Net.Security
Imports System.Security.Cryptography.X509Certificates
Imports System.Net
Imports System.IO
Imports System.Text.RegularExpressions
Imports KUAS_WiFi.SilentWebModule
Imports System.Security.Cryptography
Imports System.ComponentModel
Imports System.Xml.Serialization
Imports System.Threading

Public Class LoginFrm
    Dim JIANGONG_WIFI_SERVER As String = "172.16.61.253"
    Dim YANCHAO_WIFI_SERVER As String = "172.16.109.253"

    Public Declare Function SendMessage Lib "user32" Alias "SendMessageA" _
                            (ByVal hwnd As IntPtr,
                             ByVal wMsg As Integer,
                             ByVal wParam As IntPtr,
                             ByVal lParam As Byte()) _
                             As Integer
    Public Const EM_SETCUEBANNER As Integer = &H1501

    Public XmlPath As String = "Configs.xml"
    Dim backgroundThread As Thread

    Private Sub LoginButton_Click(sender As Object, e As EventArgs) Handles LoginButton.Click
        Try
            If (backgroundThread.IsAlive) Then
                backgroundThread.Abort()
            End If
        Catch ex As Exception

        End Try
        backgroundThread = New Thread(AddressOf Me.tryLogin)
        backgroundThread.Start()
    End Sub

    Private Sub tryLogin()
        disableViews()
        Try
            Dim response As HttpWebResponse = HttpWebResponseUtility.CreateGetHttpResponse("http://www.example.com", 7000, Nothing, Nothing)
            response.Close()
            If (response.StatusCode = 200) Then
                MsgBox("您已經登入或是有可用網路了。", MsgBoxStyle.Information, "高應無線通")
                enableViews()
            ElseIf (response.StatusCode = 302) Then
                Dim uri As New System.Uri(response.Headers.[Get]("Location"))
                login(uri.Host)
            Else
                MsgBox("發生錯誤！", MsgBoxStyle.Critical, "高應無線通")
                enableViews()
            End If
        Catch ex As Exception
            MsgBox("請求 Wi-Fi 伺服器的連線逾時。", MsgBoxStyle.Critical, "高應無線通")
            enableViews()
        End Try
    End Sub

    Private Sub LogoutButton_Click(sender As Object, e As EventArgs) Handles LogoutButton.Click
        Try
            If (backgroundThread.IsAlive) Then
                backgroundThread.Abort()
            End If
        Catch ex As Exception

        End Try
        backgroundThread = New Thread(AddressOf Me.tryLogout)
        backgroundThread.Start(True)
    End Sub

    Private Sub tryLogout(recheck As Boolean)
        disableViews()
        Try
            Dim response As HttpWebResponse = HttpWebResponseUtility.CreateGetHttpResponse("http://" + IIf(recheck, JIANGONG_WIFI_SERVER, YANCHAO_WIFI_SERVER), 7000, Nothing, Nothing)
            response.Close()
            If (response.StatusCode = 302) Then
                checkLogoutLocation(response.Headers.[Get]("Location"), recheck)
            Else
                MsgBox("登出失敗。", MsgBoxStyle.Critical, "高應無線通")
                enableViews()
            End If
        Catch ex As Exception
            MsgBox("請求 Wi-Fi 伺服器的連線逾時。", MsgBoxStyle.Critical, "高應無線通")
            enableViews()
        End Try
    End Sub

    Private Sub disableViews()
        User.Enabled = False
        Pwd.Enabled = False
        LoginButton.Enabled = False
        LogoutButton.Enabled = False
    End Sub

    Private Sub enableViews()
        User.Enabled = True
        Pwd.Enabled = True
        LoginButton.Enabled = True
        LogoutButton.Enabled = True
    End Sub

    Private Sub checkLogoutLocation(_location As String, recheck As Boolean)
         If (_location.Contains("login_online")) Then
            logout(JIANGONG_WIFI_SERVER)
        ElseIf (_location.Contains("login.php") Or _location.Contains("auth_entry.php")) Then
            If (recheck) Then
                tryLogout(False)
            Else
                MsgBox("您已經登出或是尚未登入 Wi-Fi。", MsgBoxStyle.Information, "高應無線通")
                enableViews()
            End If
        Else
            logout(YANCHAO_WIFI_SERVER)
        End If
    End Sub

    Private Sub logout(_host As String)
        Dim response As HttpWebResponse = HttpWebResponseUtility.CreateGetHttpResponse("http://" + _host + "/cgi-bin/ace_web_auth.cgi?logout", 7000, Nothing, Nothing)

        response.Close()

        If (response.StatusCode = 200) Then
            MsgBox("Wi-Fi 登出成功。", MsgBoxStyle.Information, "高應無線通")
        Else
            MsgBox("登出失敗。", MsgBoxStyle.Information, "高應無線通")
        End If

        enableViews()
    End Sub

    Private Sub login(_host As String)
        Dim parameters As IDictionary(Of String, String) = New Dictionary(Of String, String)()
        parameters.Add("username", System.Uri.EscapeDataString(User.Text))
        parameters.Add("userpwd", System.Uri.EscapeDataString(Pwd.Text))
        parameters.Add("login", "")
        parameters.Add("orig_referer", "")

        Dim response As HttpWebResponse = HttpWebResponseUtility.CreatePostHttpResponse("http://" + _host + "/cgi-bin/ace_web_auth.cgi", parameters, 7000, Nothing, Encoding.UTF8, Nothing)
        Dim reader As StreamReader = New StreamReader(response.GetResponseStream, System.Text.Encoding.GetEncoding("UTF-8"))
        Dim respHTML As String = reader.ReadToEnd()

        response.Close()

        If (respHTML.Contains("reason=")) Then
            Dim _reason As String = respHTML.Substring(respHTML.IndexOf("reason=") + 7, respHTML.IndexOf("&", respHTML.IndexOf("reason=") + 7) - respHTML.IndexOf("reason=") - 7)
            MsgBox(dumpReason(Convert.ToInt32(_reason)), MsgBoxStyle.Critical, "高應無線通")
        ElseIf (respHTML.Contains("404 - Not Found") Or response.StatusCode = 404) Then
            MsgBox("請求 Wi-Fi 伺服器的連線逾時。", MsgBoxStyle.Critical, "高應無線通")
        ElseIf (respHTML.Contains("login_online_detail.php")) Then
            If (_host.Equals(JIANGONG_WIFI_SERVER)) Then
                MsgBox("Wi-Fi 登入成功，歡迎來到高應大建工校區！", MsgBoxStyle.Information, "高應無線通")
            Else
                MsgBox("Wi-Fi 登入成功，歡迎來到高應大燕巢校區！", MsgBoxStyle.Information, "高應無線通")
            End If
        Else
            MsgBox("發生錯誤！", MsgBoxStyle.Critical, "高應無線通")
        End If

        enableViews()
    End Sub

    Private Sub LoginFrm_Load(sender As Object, e As EventArgs) Handles Me.Load
        Form.CheckForIllegalCrossThreadCalls = False
        LoadSetting()
        SendMessage(User.Handle,
                            EM_SETCUEBANNER,
                            IntPtr.Zero,
                            System.Text.Encoding.Unicode.GetBytes("學號"))
        SendMessage(Pwd.Handle,
                             EM_SETCUEBANNER,
                             IntPtr.Zero,
                             System.Text.Encoding.Unicode.GetBytes("密碼"))
    End Sub

    Private Sub LoginFrm_Closing(sender As Object, e As CancelEventArgs) Handles Me.Closing
        SaveSetting()
        Try
            If (backgroundThread.IsAlive) Then
                backgroundThread.Abort()
            End If
        Catch ex As Exception

        End Try
    End Sub

    Public Sub LoadSetting()
        If File.Exists(Application.StartupPath & "/" & XmlPath) Then
            Dim configs As BindingList(Of Config) = XmlSerialize.DeserializeFromXml(Of BindingList(Of Config))("Configs.xml")
            If Not configs.Item(0).Account = "" Then
                User.DataBindings.Add("Text", configs, "Account")
            End If
            If Not configs.Item(0).Pwd = "" Then
                Try
                    Pwd.DataBindings.Add("Text", configs, "Pwd")
                    Pwd.Text = Decrypt(Pwd.Text, "KUASWiFi")
                Catch ex As Exception

                End Try
            End If
        End If
    End Sub
    Public Sub SaveSetting()
        Dim configs As BindingList(Of Config) = Nothing
        configs = New BindingList(Of Config)()
        configs.Add(New Config() With {.Account = User.Text, .Pwd = LoginFrm.Encrypt(Pwd.Text, "KUASWiFi"), .Manager = Guid.NewGuid})
        XmlSerialize.SerializeToXml("Configs.xml", configs)
    End Sub

    Public Shared Function Encrypt(ByVal pToEncrypt As String, ByVal sKey As String) As String
        Dim des As New DESCryptoServiceProvider()
        Dim inputByteArray() As Byte
        inputByteArray = Encoding.Default.GetBytes(pToEncrypt)
        '建立加密對象的密鑰和偏移量
        '原文使用ASCIIEncoding.ASCII方法的GetBytes方法
        '使得輸入密碼必須輸入英文文本
        des.Key = ASCIIEncoding.ASCII.GetBytes(sKey)
        des.IV = ASCIIEncoding.ASCII.GetBytes(sKey)
        '寫二進制數組到加密流
        '(把內存流中的內容全部寫入)
        Dim ms As New System.IO.MemoryStream()
        Dim cs As New CryptoStream(ms, des.CreateEncryptor, CryptoStreamMode.Write)
        '寫二進制數組到加密流
        '(把內存流中的內容全部寫入)
        cs.Write(inputByteArray, 0, inputByteArray.Length)
        cs.FlushFinalBlock()

        '建立輸出字符串     
        Dim ret As New StringBuilder()
        Dim b As Byte
        For Each b In ms.ToArray()
            ret.AppendFormat("{0:X2}", b)
        Next

        Return ret.ToString()
    End Function

    '解密方法
    Public Shared Function Decrypt(ByVal pToDecrypt As String, ByVal sKey As String) As String
        Dim des As New DESCryptoServiceProvider()
        '把字符串放入byte數組
        Dim len As Integer
        len = pToDecrypt.Length / 2 - 1
        Dim inputByteArray(len) As Byte
        Dim x, i As Integer
        For x = 0 To len
            i = Convert.ToInt32(pToDecrypt.Substring(x * 2, 2), 16)
            inputByteArray(x) = CType(i, Byte)
        Next
        '建立加密對象的密鑰和偏移量，此值重要，不能修改
        des.Key = ASCIIEncoding.ASCII.GetBytes(sKey)
        des.IV = ASCIIEncoding.ASCII.GetBytes(sKey)
        Dim ms As New System.IO.MemoryStream()
        Dim cs As New CryptoStream(ms, des.CreateDecryptor, CryptoStreamMode.Write)
        cs.Write(inputByteArray, 0, inputByteArray.Length)
        cs.FlushFinalBlock()
        Return Encoding.Default.GetString(ms.ToArray)

    End Function
End Class

Namespace SilentWebModule
    ''' <summary>
    ''' 有關HTTP請求的模組
    ''' </summary>
    Public Class HttpWebResponseUtility
        Private Shared ReadOnly DefaultUserAgent As String = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0"
        ''' <summary>
        ''' 創建GET方式的HTTP請求
        ''' </summary>
        ''' <param name="url">請求的URL</param>
        ''' <param name="timeout">請求的超時時間</param>
        ''' <param name="userAgent">請求的客戶端瀏覽器信息，可以為空</param>
        ''' <param name="cookies">隨同HTTP請求發送的Cookie信息，如果不需要身分驗證可以為空</param>
        ''' <returns></returns>
        Public Shared Function CreateGetHttpResponse(url As String, timeout As System.Nullable(Of Integer), userAgent As String, cookies As CookieContainer) As HttpWebResponse
            System.Net.ServicePointManager.Expect100Continue = False '防止417

            If String.IsNullOrEmpty(url) Then
                Throw New ArgumentNullException("url")
            End If
            Dim request As HttpWebRequest = TryCast(WebRequest.Create(url), HttpWebRequest)
            request.Method = "GET"
            request.KeepAlive = True
            request.UserAgent = DefaultUserAgent
            request.AllowAutoRedirect = False

            If Not String.IsNullOrEmpty(userAgent) Then
                request.UserAgent = userAgent
            End If
            If timeout.HasValue Then
                request.Timeout = timeout.Value
            End If

            If cookies IsNot Nothing Then
                request.CookieContainer = cookies
            End If
            Return TryCast(request.GetResponse(), HttpWebResponse)
        End Function
        ''' <summary>
        ''' 創建POST方式的HTTP請求
        ''' </summary>
        ''' <param name="url">請求的URL</param>
        ''' <param name="parameters">隨同請求POST的參數名稱及參數值字典</param>
        ''' <param name="timeout">請求的超時時間</param>
        ''' <param name="userAgent">請求的客戶端瀏覽器信息，可以為空</param>
        ''' <param name="requestEncoding">發送HTTP請求時所用的編碼</param>
        ''' <param name="cookies">隨同HTTP請求發送的Cookie信息，如果不需要身分驗證可以為空</param>
        ''' <returns></returns>
        Public Shared Function CreatePostHttpResponse(url As String, parameters As IDictionary(Of String, String), timeout As System.Nullable(Of Integer), userAgent As String, requestEncoding As Encoding, cookies As CookieContainer) As HttpWebResponse
            System.Net.ServicePointManager.Expect100Continue = False '防止417

            If String.IsNullOrEmpty(url) Then
                Throw New ArgumentNullException("url")
            End If
            If requestEncoding Is Nothing Then
                Throw New ArgumentNullException("requestEncoding")
            End If
            Dim request As HttpWebRequest = Nothing
            '如果是發送HTTPS請求
            If url.StartsWith("https", StringComparison.OrdinalIgnoreCase) Then
                ServicePointManager.ServerCertificateValidationCallback = New RemoteCertificateValidationCallback(AddressOf CheckValidationResult)
                request = TryCast(WebRequest.Create(url), HttpWebRequest)
                request.ProtocolVersion = HttpVersion.Version11
            Else
                request = TryCast(WebRequest.Create(url), HttpWebRequest)
            End If
            request.Method = "POST"
            request.KeepAlive = True
            request.ContentType = "application/x-www-form-urlencoded"
            request.AllowAutoRedirect = False

            If Not String.IsNullOrEmpty(userAgent) Then
                request.UserAgent = userAgent
            Else
                request.UserAgent = DefaultUserAgent
            End If

            If timeout.HasValue Then
                request.Timeout = timeout.Value
            End If
            If cookies IsNot Nothing Then
                request.CookieContainer = cookies
            End If
            '如果需要POST數據
            If Not (parameters Is Nothing OrElse parameters.Count = 0) Then
                Dim buffer As New StringBuilder()
                Dim i As Integer = 0
                For Each key As String In parameters.Keys
                    If i > 0 Then
                        buffer.AppendFormat("&{0}={1}", key, parameters(key))
                    Else
                        buffer.AppendFormat("{0}={1}", key, parameters(key))
                    End If
                    i += 1
                Next
                Dim data As Byte() = requestEncoding.GetBytes(buffer.ToString())
                Using stream As Stream = request.GetRequestStream()
                    stream.Write(data, 0, data.Length)
                End Using
            End If
            Return TryCast(request.GetResponse(), HttpWebResponse)

        End Function

        Private Shared Function CheckValidationResult(sender As Object, certificate As X509Certificate, chain As X509Chain, errors As SslPolicyErrors) As Boolean
            Return True
            '總是接受
        End Function
    End Class
End Namespace

Namespace SystemAPI.Function.EncryptLibrary
    Public Class EncryptSHA
        ''' <summary>
        ''' 使用SHA加密訊息
        ''' </summary>
        ''' <param name="sourceMessage">原始資訊</param>
        ''' <param name="SHAType">SHA加密方式</param>
        ''' <returns>string</returns>
        Public Function Encrypt(sourceMessage As String, SHAType As EnumSHAType) As String
            If String.IsNullOrEmpty(sourceMessage) Then
                Return String.Empty
            End If

            '字串先轉成byte[]
            Dim Message As Byte() = Encoding.Unicode.GetBytes(sourceMessage)
            Dim HashImplement As HashAlgorithm = Nothing

            '選擇要使用的SHA加密方式
            Select Case SHAType
                Case EnumSHAType.SHA1
                    HashImplement = New SHA1Managed()
                    Exit Select
                Case EnumSHAType.SHA256
                    HashImplement = New SHA256Managed()
                    Exit Select
                Case EnumSHAType.SHA384
                    HashImplement = New SHA384Managed()
                    Exit Select
                Case EnumSHAType.SHA512
                    HashImplement = New SHA512Managed()
                    Exit Select
            End Select

            '取Hash值
            Dim HashValue As Byte() = HashImplement.ComputeHash(Message)

            '把byte[]轉成string後，再回傳
            Return BitConverter.ToString(HashValue).Replace("-", "").ToLower()

        End Function

        Public Enum EnumSHAType
            SHA1
            SHA256
            SHA384
            SHA512
        End Enum

    End Class
End Namespace
Public Class XmlSerialize
    Public Shared Sub SerializeToXml(FileName As String, [Object] As Object)
        Dim xml As XmlSerializer = Nothing
        Dim stream As Stream = Nothing
        Dim writer As StreamWriter = Nothing
        Try
            xml = New XmlSerializer([Object].[GetType]())
            stream = New FileStream(FileName, FileMode.Create, FileAccess.Write, FileShare.Read)
            writer = New StreamWriter(stream, Encoding.UTF8)
            xml.Serialize(writer, [Object])
        Catch ex As Exception
            Throw ex
        Finally
            writer.Close()
            stream.Close()
        End Try
    End Sub
    Public Shared Function DeserializeFromXml(Of T)(FileName As String) As T
        Dim xml As XmlSerializer = Nothing
        Dim stream As Stream = Nothing
        Dim reader As StreamReader = Nothing
        Try
            xml = New XmlSerializer(GetType(T))
            stream = New FileStream(FileName, FileMode.Open, FileAccess.Read, FileShare.None)
            reader = New StreamReader(stream, Encoding.UTF8)
            Dim obj As Object = xml.Deserialize(reader)
            If obj Is Nothing Then
                Return Nothing
            Else
                Return DirectCast(obj, T)
            End If
        Catch ex As Exception
            Throw ex
        Finally
            stream.Close()
            reader.Close()
        End Try
    End Function
End Class
