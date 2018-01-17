package si.fri.rso.projekt;

public class Message {
    private String apartmentId;
    private String userId;

    private String key;
    private String content;
    private String topic;

    public Message(){

    }

    public Message(String apartmentId, String userId, String content){
        this.apartmentId = apartmentId;
        this.userId = userId;

        this.content = content;
        this.topic = "lbu290s1-default";
        setValuesToKey();
    }

    public void setMessageFromConsumerRecord (String key, String content, String topic){
        this.key = key;
        this.content = content;
        this.topic = topic;

        setValuesFromKey();
    }

    private String breaker(){
        return "_ _";
    }

    private void setValuesToKey(){
        if(this.apartmentId!=null ) this.key = this.apartmentId+ breaker() +this.userId;
    }

    private void setValuesFromKey(){
        String[] keys = this.key.split(breaker());
        if (keys.length > 1){
            this.apartmentId = keys[0];
            this.userId = keys[1];
        } else{
            this.apartmentId = null;
            this.userId = null;
        }
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}