package il.ac.bgu.cs.bp.bpjs.myAbp;

public class L3Msg {
    private int secNo;
    private String data;

    public L3Msg(int secNo, String data) {
        this.secNo = secNo;
        this.data = data;

    }
    public L3Msg(int secNo) {
        this.secNo = secNo;
        this.data = "Dummy";
    }
    public int getSecNo() {
        return secNo;
    }

    public String getData() {
        return data;
    }

    public void setSecNo(int secNo) {
        this.secNo = secNo;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "L3Msg{" +
                "secNo=" + secNo +
                ", data='" + data + '\'' +
                '}';
    }
}
