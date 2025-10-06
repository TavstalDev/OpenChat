package io.github.tavstaldev.openChat;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;

import java.util.*;

public class OpenChatConfiguration extends ConfigurationBase {

    public OpenChatConfiguration() {
        super(OpenChat.Instance, "config.yml", null);
    }

    // General
    public String locale, prefix;
    public boolean usePlayerLocale, checkForUpdates, debug;

    // Storage
    public String storageType, storageFilename, storageHost, storageDatabase, storageUsername, storagePassword, storageTablePrefix;
    public int storagePort;

    // Anti-Spam
    public boolean antiSpamEnabled;
    public int antiSpamChatDelay, antiSpamCommandDelay, antiSpamMaxDuplicates, antiSpamMaxCommandDuplicates;
    public Set<String> antiSpamCommandWhitelist;
    public Set<String> antiSpamExecuteCommand;
    public String antiSpamExemptPermission;

    // Anti-Advertisement
    public boolean antiAdvertisementEnabled;
    public String antiAdvertisementRegex;
    public Set<String> antiAdvertisementWhitelist;
    public Set<String> antiAdvertisementExecuteCommand;
    public String antiAdvertisementExemptPermission;

    // Anti-Caps
    public boolean antiCapsEnabled;
    public int antiCapsMinLength, antiCapsPercentage;
    public Set<String> antiCapsExecuteCommand;
    public String antiCapsExemptPermission;

    // Anti-Swear
    public boolean antiSwearEnabled;
    // Character mapping and bad words are not stored here, since they are only called
    // when initializing the AntiSwearSystem class, so storing them here would be redundant.
    public Set<String> antiSwearExecuteCommand;
    public String antiSwearExemptPermission;

    // Command Blocker
    public boolean commandBlockerEnabled;
    public boolean commandBlockerEnableBypass;
    public String commandBlockerBypassPermission;
    public Set<String> commandBlockerCommands;

    // Tab completion
    public boolean tabCompletionEnabled;
    public String tabCompletionExemptPermission;

    // OP-Protection
    public boolean opProtectionEnabled;
    public Set<String> opProtectionOperators;

    // Private Messaging
    public boolean privateMessagingEnabled;

    // Custom Chat
    public boolean customChatEnabled;
    public String customChatFormat;
    public String customChatShoutFormat;
    public String customChatQuestionFormat;
    public boolean customChatShoutEnabled;
    public String customChatShoutPermission;
    public String customChatShoutPrefix;
    public boolean customChatQuestionEnabled;
    public String customChatQuestionPermission;
    public String customChatQuestionPrefix;
    public String customChatLegacyRichTextPermission;
    public String customChatHexRichTextPermission;

    // Mentions
    public boolean mentionsEnabled;
    public String mentionsDefaultDisplay, mentionsDefaultPreference, mentionsDefaultSound;
    public double mentionsVolume, mentionsPitch;
    public int mentionsCooldown, mentionsLimitPerMessage;
    public boolean mentionsAllowSelfMention;

    @Override
    protected void loadDefaults() {
        // General
        locale = resolveGet("locale", "hun");
        usePlayerLocale = resolveGet("usePlayerLocale", true);
        checkForUpdates = resolveGet("checkForUpdates", true);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&bOpen&3Chat &8»");

        // Storage
        storageType = resolveGet("storage.type", "sqlite");
        storageFilename = resolveGet("storage.filename", "database");
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "openchat");

        // Anti-Spam
        antiSpamEnabled = resolveGet("antiSpam.enabled", true);
        antiSpamChatDelay = resolveGet("antiSpam.chatDelay", 2);
        antiSpamMaxDuplicates= resolveGet("antiSpam.maxDuplicates", 3);
        antiSpamCommandDelay = resolveGet("antiSpam.commandDelay", 2);
        antiSpamMaxCommandDuplicates= resolveGet("antiSpam.maxCommandDuplicates", 3);
        antiSpamCommandWhitelist = new LinkedHashSet<>(resolveGet("antiSpam.commandWhitelist", List.of(
                "/msg",
                "/tell",
                "/w",
                "/r",
                "/reply",
                "/t",
                "/me",
                "/whisper",
                "/warp",
                "/warps",
                "/home",
                "/spawn",
                "/joinqueue",
                "/leavequeue",
                "/kit",
                "/kits",
                "/party"
        )));
        antiSpamExemptPermission = resolveGet("antiSpam.exemptPermission", "openchat.bypass.antispam");
        antiSpamExecuteCommand =  new LinkedHashSet<>(resolveGet("antiSpam.executeCommand", List.of("kick {player} Please do not spam")));


        // Anti-Advertisement
        antiAdvertisementEnabled = resolveGet("antiAdvertisement.enabled", true);
        antiAdvertisementRegex = resolveGet("antiAdvertisement.regex", "(?i)\\b((?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+(?:[a-z]{2,}))\\b");
        antiAdvertisementWhitelist = new LinkedHashSet<>(resolveGet("antiAdvertisement.whitelist", List.of(
                "mestermc.hu",
                "discord.gg/mestermc",
                "youtube.com/mestermc",
                "facebook.com/mestermc",
                "tiktok.com/@mestermc"
        )));
        antiAdvertisementExemptPermission = resolveGet("antiAdvertisement.exemptPermission", "openchat.bypass.antiadvertisement");
        antiAdvertisementExecuteCommand = new LinkedHashSet<>(resolveGet("antiAdvertisement.executeCommand", List.of("kick {player} Please do not advertise")));

        // Anti-Caps
        antiCapsEnabled = resolveGet("antiCaps.enabled", true);
        antiCapsMinLength = resolveGet("antiCaps.minLength", 10);
        antiCapsPercentage = resolveGet("antiCaps.percentage", 70);
        antiCapsExemptPermission = resolveGet("antiCaps.exemptPermission", "openchat.bypass.anticaps");
        antiCapsExecuteCommand = new LinkedHashSet<>(resolveGet("antiCaps.executeCommand", List.of("kick {player} Please do not spam")));

        // Anto-swear
        antiSwearEnabled = resolveGet("antiSwear.enabled", true);
        Map<Character, String> characterMappings = new LinkedHashMap<>();
        characterMappings.put('a', "[aA@4]");
        characterMappings.put('á', "[áÁaA@4]");
        characterMappings.put('e', "[eE3]");
        characterMappings.put('i', "[iI1!íÍ]");
        characterMappings.put('o', "[oO0óÓ]");
        characterMappings.put('s', "[sS5$]");
        characterMappings.put('u', "[uUúÚ]");
        characterMappings.put('ü', "[üÜűŰuU]");
        characterMappings.put('t', "[tT7+]");
        characterMappings.put('g', "[gG9]");
        characterMappings.put('b', "[bB8]");
        characterMappings.put('z', "[zZ2]");
        resolveGet("antiSwear.characterMapping", characterMappings);
        //#region Swear words
        resolveGet("antiSwear.swearWords", new String[]{
                "asshole",
                "bitch",
                "bastard",
                "cunt",
                "damn",
                "dick",
                "fuck",
                "hell",
                "piss",
                "pussy",
                "retard",
                "shit",
                "slut",
                "twat",
                "whore",
                "chink",
                "dyke",
                "fag",
                "faggot",
                "gook",
                "kike",
                "nigger",
                "spic",
                "tranny",
                "wetback",
                "blowjob",
                "cock",
                "ejaculate",
                "jerkoff",
                "masturbate",
                "orgasm",
                "penis",
                "semen",
                "vagina",
                "aids-es",
                "agyatlan",
                "agybatetovált",
                "agyfasz",
                "agyhalott",
                "agyonkúrt",
                "agyonvert",
                "agyrákos",
                "animalsex-mániás",
                "antibaro",
                "anyadat",
                "anyatokkal",
                "aprófaszú",
                "arcbarakott",
                "aszaltfaszú",
                "átbaszott",
                "ágybavizelős",
                "balfasz",
                "balfészek",
                "baromfifasz",
                "basz-o-matic",
                "baszál",
                "baszad",
                "baszhatatlan",
                "baszlak",
                "basznivaló",
                "basszál",
                "basszodj",
                "bebaszott",
                "befosi",
                "békapicsa",
                "bélböfi",
                "beleiből kiforgatott",
                "bélszél",
                "bronz térdű",
                "brunya",
                "büdös szájú",
                "büdösszájú",
                "búvalbaszott",
                "buzeráns",
                "buzernyák",
                "buzi",
                "buzikurva",
                "cafat",
                "cafka",
                "céda",
                "cérnafaszú",
                "cigány",
                "cici",
                "cottonfej",
                "cornhub",
                "csaf",
                "cseszett",
                "csibefasz",
                "csicska",
                "csipszar",
                "csirkefaszú",
                "csitri",
                "csöcs",
                "csöcsfej",
                "csöppszar",
                "csöves",
                "csupaszfarkú",
                "cuncipunci",
                "deformáltfaszú",
                "dekorált pofájú",
                "degenerált",
                "dobseggű",
                "döbbenetesen segg",
                "drogos",
                "drogozni",
                "drugs",
                "drug",
                "dughatatlan",
                "dunyhavalagú",
                "duplafaszú",
                "ebfasz",
                "eki",
                "ekidzseki",
                "elbaszott",
                "eleve hülye",
                "extrahülye",
                "fafogú rézfűrésszel megsebzett",
                "fasszopó",
                "fasz",
                "fasz-emulátor",
                "faszagyú",
                "faszarc",
                "faszfej",
                "faszfészek",
                "faszkalap",
                "faszk-arika",
                "faszkedvelő",
                "faszkópé",
                "faszogány",
                "faszpörgettyű",
                "faszsapka",
                "faszszagú",
                "fasztalan",
                "fasztarisznya",
                "fasztengely",
                "fasztolvaj",
                "faszváladék",
                "faszverő",
                "félrebaszott",
                "félrefingott",
                "félreszart",
                "félribanc",
                "féreg",
                "fing",
                "fityma",
                "fölcsinált",
                "fölfingott",
                "fos",
                "foskemence",
                "fospisztoly",
                "fospumpa",
                "fostalicska",
                "fütyi",
                "fütyinyalogató",
                "fütykös",
                "geci",
                "gecinyelő",
                "geciszaró",
                "geciszívó",
                "genny",
                "gennyesszájú",
                "gennygóc",
                "genyac",
                "genyó",
                "gerinctelen",
                "gólyafos",
                "görbefaszú",
                "gyennyszopó",
                "gyíkfing",
                "hájpacni",
                "halj éhen",
                "halj ki",
                "halj meg",
                "hatalmas nagy fasz",
                "hátbabaszott",
                "házikurva",
                "hererákos",
                "hígagyú",
                "hihetetlenül fasz",
                "hikomat",
                "hímnőstény",
                "hímringyó",
                "hímvesző",
                "hímveszővel",
                "hiperstrici",
                "hitler",
                "hitler-imádó",
                "hitlerista",
                "hivatásos balfasz",
                "hú de segg",
                "hugyagyú",
                "hugyos",
                "hugytócsa",
                "hüje",
                "hülye",
                "hülyécske",
                "hülyegyerek",
                "idióta",
                "inkubátor-szökevény",
                "integrált barom",
                "ionizált faszú",
                "iq bajnok",
                "iq fighter",
                "iq hiányos",
                "irdatlanul köcsög",
                "íveltfaszú",
                "jajj de barom",
                "joint",
                "jókora fasz",
                "kaka",
                "kakamatyi",
                "kaki",
                "kaksi",
                "kecskebaszó",
                "kellően fasz",
                "képlékeny faszú",
                "keresve sem található fasz",
                "kétfaszú",
                "kétszer agyonbaszott",
                "keys",
                "ki-bebaszott",
                "kibaszott",
                "kifingott",
                "kiherélt",
                "kikakkantott",
                "kikészült",
                "kimagaslóan fasz",
                "kimondhatatlan pöcs",
                "kis szaros",
                "kisfütyi",
                "kill yourself",
                "klotyószagú",
                "kokain",
                "ködmönbe bújtatott",
                "koj",
                "kopárfaszú",
                "korlátolt gecizésű",
                "kotonszökevény",
                "középszar",
                "köcsög",
                "kretén",
                "kuki",
                "kula",
                "kunkorított faszú",
                "kurva",
                "kurvaanyjú",
                "kurvapecér",
                "kurlak",
                "kutyakaki",
                "kutyapina",
                "kutyaszar",
                "lankadtfaszú",
                "lebaszirgált",
                "lebaszott",
                "lecseszett",
                "leírhatatlanul segg",
                "lemenstruált",
                "leokádott",
                "lepkefing",
                "leprafészek",
                "leszart",
                "leszbikus",
                "lőcs",
                "lőcsgéza",
                "lófasz",
                "lógócsöcsű",
                "lóhugy",
                "lotyó",
                "lucskos",
                "lugnya",
                "lyukasbelű",
                "lyukasfaszú",
                "lyukát vakaró",
                "lyuktalanított",
                "mamutsegg",
                "mangalica",
                "marihuana",
                "marihónaja",
                "maszturbációs görcs",
                "maszturbagép",
                "maszturbáltatott",
                "megbaszlak",
                "megfingatott",
                "megkettyintett",
                "megkúrt",
                "megkurlak",
                "megszopatott",
                "mesterséges faszú",
                "metamphetamine",
                "méteres kékeres",
                "meth",
                "mikrotökű",
                "mocsok",
                "mocskod",
                "mocskos",
                "mojfing",
                "műfaszú",
                "muff",
                "multifasz",
                "műtöttpofájú",
                "náci",
                "nagymellek",
                "nagyfejű",
                "nikotinpatkány",
                "nimfomániás",
                "nuna",
                "nunci",
                "nuncóka",
                "nyalábfasz",
                "nyalj ki",
                "nyalj meg",
                "nyelestojás",
                "nyomorék",
                "nyúlszar",
                "oltári nagy fasz",
                "ondónyelő",
                "orbitálisan hülye",
                "ordenálé",
                "összebaszott",
                "ötcsillagos fasz",
                "ótvaros",
                "óvszerezett",
                "pénisz",
                "peremesfaszú",
                "picsa",
                "picsafej",
                "picsameresztő",
                "picsánnyalt",
                "picsánrugott",
                "picsányi",
                "pikkelypáncélt hordó",
                "pina",
                "pinés",
                "pinuja",
                "pisa",
                "pisaszagú",
                "pisis",
                "pöcs",
                "pöcsfej",
                "porbafingó",
                "porno",
                "pornó",
                "pornóbuzi",
                "pornómániás",
                "poresz",
                "pudvás",
                "pudváslikú",
                "puhafaszú",
                "punci",
                "puncimókus",
                "puncis",
                "punciutáló",
                "puncivirág",
                "puresz",
                "qtyaszar",
                "qki",
                "qrva",
                "rákos",
                "rák a beled",
                "rabló",
                "rágcsáltfaszú",
                "redva",
                "rendkívül fasz",
                "repedtsarkú",
                "retek",
                "retkes",
                "rétó-román",
                "rézhasú",
                "ribanc",
                "riherongy",
                "ritka fogú",
                "rivalizáló",
                "rohadj",
                "rojtospicsájú",
                "rongyospinájú",
                "roppant hülye",
                "rossz kurva",
                "rosszlányok",
                "rószlányok",
                "rosszlanyok",
                "rőfös fasz",
                "saját nemével kefélő",
                "segg",
                "seggarc",
                "seggdugó",
                "seggfej",
                "seggnyaló",
                "seggszőr",
                "seggtorlasz",
                "sikoltozásokba öltöztetett",
                "strici",
                "suttyó",
                "sutyerák",
                "szálkafaszú",
                "szar",
                "szaralak",
                "szarbojler",
                "szarcsimbók",
                "szarevő",
                "szarfaszú",
                "szarházi",
                "szarjankó",
                "szarnivaló",
                "szarosvalagú",
                "szarrá vágott",
                "szarrágó",
                "szarszagú",
                "szarszájú",
                "szartragacs",
                "szarzsák",
                "szárazfing",
                "szégyencsicska",
                "szifiliszes",
                "szivattyús kurva",
                "szop-o-matic",
                "szopás",
                "szopd",
                "szopi le",
                "szopj",
                "szopógép",
                "szopógörcs",
                "szopós kurva",
                "szopottfarkú",
                "szófosó",
                "szokatlanul fasz",
                "szuperbuzi",
                "szuperkurva",
                "szűklyukú",
                "szultán udvarát megjárt",
                "szúnyogfaszni",
                "szűzhártya-repedéses",
                "szűzkurva",
                "szűzpicsa",
                "szűzpunci",
                "tetves",
                "tikfos",
                "tikszar",
                "tompátökű",
                "toszatlan",
                "toszott",
                "totálisan hülye",
                "törpefaszú",
                "tyúkfasznyi",
                "tyúkszar",
                "tyű de picsa",
                "vadfasz",
                "valag",
                "valagváladék",
                "végbélféreg",
                "xar",
                "xvideos",
                "xxx",
                "zsíragy",
                "zsugorított faszú",
                "hitler",
                "basszodj",
                "retkes",
                "ótvaros",
                "cigány",
                "kurva",
                "nyomorék",
                "basszad",
                "rákos",
                "cici",
                "rohadj",
                "baszál",
                "basszál",
                "anyadat",
                "pinuja",
                "idiota",
                "idióta",
                "fasztalan",
                "fityma",
                "megbaszlak",
                "megkurlak",
                "baszlak",
                "kurlak",
                "retek",
                "gerinctelen",
                "féreg",
                "szopás",
                "köcsög",
                "pinés",
                "szopi le",
                "csöves",
                "mangalica",
                "hímvesző",
                "hímveszővel",
                "szopd",
                "szopj",
                "halj meg",
                "halj ki",
                "porno",
                "pornó",
                "puresz",
                "poresz",
                "rák a beled",
                "csicska",
                "agyatlan",
                "mocsok",
                "mocskod",
                "anyatokkal",
                "halj éhen",
                "degenerált",
                "dagadék",
                "dagadt",
                "zsíragy",
                "keys",
                "kill yourself",
                "xvideos",
                "pornhub",
                "cornhub",
                "xxx",
                "nagymellek",
                "rosszlányok",
                "rószlányok",
                "rosszlanyok",
                "drug",
                "drugs",
                "drogos",
                "drogozni",
                "kokain",
                "anfetamin",
                "joint",
                "meth",
                "marihuana",
                "marihónaja",
                "metamphetamine",
                "ekidzseki",
                "nyalj ki",
                "nyalj meg",
                "nigga",
                "néger",
                "degenerált",
                "degen",
                "bazd",
                "bazmeg",
                "bazdmeg",
                "bazd meg"
        });
        //#endregion
        //#region Swear whitelist
        resolveGet("antiSwear.whitelist", new String[]{
                "hello",
                "helló",
                "shuttle",
                "görbe",
                "gyerek",
                "kicsi",
                "szarvas",
                "szarvasbogár",
                "szemét",
                "szeméttelep",
                "szifon",
                "szégyen",
                "szégyentelen",
                "tökös",
                "törpe",
                "szeretek"
        });
        //#endregion
        antiSwearExemptPermission = resolveGet("antiSwear.exemptPermission", "openchat.bypass.antiswear");
        antiSwearExecuteCommand = new LinkedHashSet<>(resolveGet("antiSwear.executeCommand", List.of("kick {player} Please do not swear")));

        // Command Blocker
        commandBlockerEnabled = resolveGet("commandBlocker.enabled", true);
        commandBlockerEnableBypass = resolveGet("commandBlocker.enableBypass", true);
        commandBlockerBypassPermission = resolveGet("commandBlocker.bypassPermission", "openchat.bypass.commandblocker");
        commandBlockerCommands = new LinkedHashSet<>(resolveGet("commandBlocker.commands", List.of(
                "/?",
                "/version",
                "/ver",
                "/icanhasbukkit",
                "/about",
                "/pl",
                "/plugins",
                "/bukkit:pl",
                "/bukkit:plugins",
                "/bukkit:version",
                "/bukkit:ver",
                "/bukkit:about",
                "/bukkit:icanhasbukkit",
                "/bukkit: null",
                "/minecraft: null",
                "/minecraft:me",
                "/minecraft:tell",
                "/minecraft",
                "/minecraft:op",
                "/minecraft:pardon-ip",
                "/minecraft:pardon",
                "/calculate",
                "//calculate",
                "//eval",
                "/eval",
                "//evalaute",
                "/evaluate",
                "//solve",
                "/solve",
                "/reload",
                "/stop",
                "/op",
                "/execute",
                "/sudo",
                "/say",
                "/pt bc",
                "/bc"
                )));


        // Tab completion
        tabCompletionEnabled = resolveGet("tabCompletion.enabled", true);
        tabCompletionExemptPermission = resolveGet("tabCompletion.exemptPermission", "openchat.bypass.tabcompletion");
        if (get("tabCompletion.entries") == null) {
            var tabEntriesDefault = new LinkedHashMap<String, Object>();
            var defaultEntries = new LinkedHashMap<String, Object>();
            defaultEntries.put("priority", 0);
            defaultEntries.put("commands", List.of(
                    "/is",
                    "/island",
                    "/sellwands",
                    "/spawners",
                    "/warp",
                    "/warps",
                    "/team",
                    "/balance",
                    "/bal",
                    "/home",
                    "/sethome",
                    "/delhome",
                    "/ignore",
                    "/emojis",
                    "/kit",
                    "/kits",
                    "/list",
                    "/msg",
                    "/msgtoggle",
                    "/pay",
                    "/realname",
                    "/spawn",
                    "/suicide",
                    "/tpa",
                    "/tpaccept",
                    "/tpdeny",
                    "/tpahere",
                    "/tptoggle",
                    "/pwarp",
                    "/playerwarps",
                    "/pw",
                    "/ah",
                    "/auction",
                    "/rewards",
                    "/coinshop",
                    "/help",
                    "/tutorial",
                    "/serverguide",
                    "/worlds",
                    "/rtp",
                    "/toolskins",
                    "/leaderboards",
                    "/lottery",
                    "/deliveries",
                    "/levels",
                    "/shop",
                    "/jobs",
                    "/quests",
                    "/skills",
                    "/factories",
                    "/cosmetics",
                    "/teams",
                    "/dailyquests",
                    "/tags",
                    "/settings",
                    "/discord",
                    "/store",
                    "/vote",
                    "/website",
                    "/referral",
                    "/rules",
                    "/nick",
                    "/back",
                    "/recipe",
                    "/feed",
                    "/disposal",
                    "/near",
                    "/craft",
                    "/enderchest",
                    "/ptime",
                    "/heal",
                    "/fly",
                    "/pweather",
                    "/repair",
                    "/invsee",
                    "/sellall",
                    "/sellhand",
                    "/enchants"
            ));

            var staffEntries = new LinkedHashMap<String, Object>();
            staffEntries.put("priority", 1);
            staffEntries.put("extend", "default");
            staffEntries.put("commands", List.of(
                    "/ban",
                    "/banip",
                    "/unban",
                    "/unbanip",
                    "/pardon",
                    "/pardonip",
                    "/banlist",
                    "/changereason",
                    "/check",
                    "/history",
                    "/kick",
                    "/mute"
            ));

            tabEntriesDefault.put("default", defaultEntries);
            tabEntriesDefault.put("staff", staffEntries);
            resolve("tabCompletion.entries", tabEntriesDefault);
        }

        // OP-Protection
        opProtectionEnabled = resolveGet("opProtection.enabled", false);
        opProtectionOperators = new LinkedHashSet<>(resolveGet("opProtection.operators", List.of(
                "Steve",
                "Alex",
                "Tavstal"
        )));

        // Private Messaging
        privateMessagingEnabled = resolveGet("privateMessaging.enabled", true);

        // Custom Chat
        customChatEnabled = resolveGet("customChat.enabled", false);
        customChatFormat = resolveGet("customChat.format", "<{player}> {message}");
        customChatShoutEnabled = resolveGet("customChat.shoutEnabled", true);
        customChatShoutFormat = resolveGet("customChat.shoutFormat", "[SHOUT] <{player}> {message}");
        customChatShoutPermission = resolveGet("customChat.shoutPermission", "openchat.chat.shout");
        customChatShoutPrefix = resolveGet("customChat.shoutPrefix", "!");
        customChatQuestionEnabled = resolveGet("customChat.questionEnabled", true);
        customChatQuestionFormat = resolveGet("customChat.questionFormat", "[QUESTION] <{player}> {message}");
        customChatQuestionPermission = resolveGet("customChat.questionPermission", "openchat.chat.question");
        customChatQuestionPrefix = resolveGet("customChat.questionPrefix", "?");
        customChatLegacyRichTextPermission = resolveGet("customChat.legacyRichTextPermission", "openchat.chat.color");
        customChatHexRichTextPermission = resolveGet("customChat.hexRichTextPermission", "openchat.chat.hexcolor");

        // Mentions
        mentionsEnabled = resolveGet("mentions.enabled", true);
        mentionsDefaultDisplay = resolveGet("mentions.defaultDisplay", "ALL");
        mentionsDefaultPreference = resolveGet("mentions.defaultPreference", "ALWAYS");
        mentionsDefaultSound = resolveGet("mentions.defaultSound", "ENTITY_PLAYER_LEVELUP");
        mentionsVolume = resolveGet("mentions.volume", 1.0);
        mentionsPitch = resolveGet("mentions.pitch", 1.0);
        mentionsCooldown = resolveGet("mentions.mentionCooldown", 3);
        mentionsLimitPerMessage = resolveGet("mentions.maxMentionsPerMessage", 3);
        mentionsAllowSelfMention = resolveGet("mentions.allowSelfMention", true);
    }
}
