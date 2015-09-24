' Developer - Silent
' Date Created - 02/24/2014
'
' General Description - Simple Config class used for Business Object examples

Public Class Config

#Region " Modular Variables "

    Private _Account As String
    Private _Pwd As String
    Private _Consignee As String
    Private _City As String
    Private _District As String
    Private _Address As String
    Private _ZipCode As String
    Private _Tel As String
    Private _Amount As String
    Private _Timeout As String
    Private _DianYuan As String
    Private _Bank As String
    Private _BestTime As String
    Private _ChooseItem As String
    Private _Mode As String
    Private _TimeCheck As String
    Private _Time As String
    Private _Manager As Guid

#End Region

#Region " Constructors "

    Public Sub New()

    End Sub

#End Region

#Region " Public Properties "

    ''' <summary>
    ''' Property to hold the Account of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Account</returns>
    Public Property Account() As String
        Get
            Return _Account
        End Get
        Set(ByVal value As String)
            _Account = value
            'Console.WriteLine("Property {0} has been changed.", "Account")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Pwd of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Pwd</returns>
    Public Property Pwd() As String
        Get
            Return _Pwd
        End Get
        Set(ByVal value As String)
            _Pwd = value
            'Console.WriteLine("Property {0} has been changed.", "Pwd")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the City of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config City</returns>
    Public Property City() As String
        Get
            Return _City
        End Get
        Set(ByVal value As String)
            _City = value
            'Console.WriteLine("Property {0} has been changed.", "City")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Consignee of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Consignee</returns>
    Public Property Consignee() As String
        Get
            Return _Consignee
        End Get
        Set(ByVal value As String)
            _Consignee = value
            'Console.WriteLine("Property {0} has been changed.", "Consignee")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Tel of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Tel</returns>
    Public Property Tel() As String
        Get
            Return _Tel
        End Get
        Set(ByVal value As String)
            _Tel = value
            'Console.WriteLine("Property {0} has been changed.", "Tel")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the District of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config District</returns>
    Public Property District() As String
        Get
            Return _District
        End Get
        Set(ByVal value As String)
            _District = value
            'Console.WriteLine("Property {0} has been changed.", "District")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the ZipCode of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config ZipCode</returns>
    Public Property ZipCode() As String
        Get
            Return _ZipCode
        End Get
        Set(ByVal value As String)
            _ZipCode = value
            'Console.WriteLine("Property {0} has been changed.", "ZipCode")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Address of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Address</returns>
    Public Property Address() As String
        Get
            Return _Address
        End Get
        Set(ByVal value As String)
            _Address = value
            'Console.WriteLine("Property {0} has been changed.", "Address")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Amount of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Amount</returns>
    Public Property Amount() As String
        Get
            Return _Amount
        End Get
        Set(ByVal value As String)
            _Amount = value
            'Console.WriteLine("Property {0} has been changed.", "Amount")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Timeout of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Timeout</returns>
    Public Property Timeout() As String
        Get
            Return _Timeout
        End Get
        Set(ByVal value As String)
            _Timeout = value
            'Console.WriteLine("Property {0} has been changed.", "Timeout")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the DianYuan of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config DianYuan</returns>
    Public Property DianYuan() As String
        Get
            Return _DianYuan
        End Get
        Set(ByVal value As String)
            _DianYuan = value
            'Console.WriteLine("Property {0} has been changed.", "DianYuan")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Bank of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Bank</returns>
    Public Property Bank() As String
        Get
            Return _Bank
        End Get
        Set(ByVal value As String)
            _Bank = value
            'Console.WriteLine("Property {0} has been changed.", "Bank")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the BestTime of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config BestTime</returns>
    Public Property BestTime() As String
        Get
            Return _BestTime
        End Get
        Set(ByVal value As String)
            _BestTime = value
            'Console.WriteLine("Property {0} has been changed.", "BestTime")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the ChooseItem of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config ChooseItem</returns>
    Public Property ChooseItem() As String
        Get
            Return _ChooseItem
        End Get
        Set(ByVal value As String)
            _ChooseItem = value
            'Console.WriteLine("Property {0} has been changed.", "ChooseItem")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Mode of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Mode</returns>
    Public Property Mode() As String
        Get
            Return _Mode
        End Get
        Set(ByVal value As String)
            _Mode = value
            'Console.WriteLine("Property {0} has been changed.", "Mode")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the TimeCheck of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config TimeCheck</returns>
    Public Property TimeCheck() As String
        Get
            Return _TimeCheck
        End Get
        Set(ByVal value As String)
            _TimeCheck = value
            'Console.WriteLine("Property {0} has been changed.", "Mode")
        End Set
    End Property

    ''' <summary>
    ''' Property to hold the Time of the config
    ''' </summary>
    ''' <value>String</value>
    ''' <returns>Config Time</returns>
    Public Property Time() As String
        Get
            Return _Time
        End Get
        Set(ByVal value As String)
            _Time = value
            'Console.WriteLine("Property {0} has been changed.", "Mode")
        End Set
    End Property

    ''' <summary>
    ''' Holds config managers ID
    ''' </summary>
    ''' <value>Guid</value>
    ''' <returns>Config managers Guid</returns>
    Public Property Manager() As Guid
        Get
            Return _Manager
        End Get
        Set(ByVal value As Guid)
            _Manager = value
            'Console.WriteLine("Property {0} has been changed.", "Manager")
        End Set
    End Property

#End Region

End Class
