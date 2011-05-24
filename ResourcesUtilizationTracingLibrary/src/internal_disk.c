/*
 * internal_disk.c
 *
 *  Created on: Mar 17, 2011
 *      Author: Timo Minartz
 *
 *  This file is completely based on extracts of hdparm-9.37 orinally written by Mark Lord (C) 1994-2008 -- freely distributable
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <scsi/sg.h>
#include <linux/types.h> /* for __u8 */
#include <errno.h> /* for errno */
#include <string.h> /* for memset */
#include <sys/ioctl.h> /* for ioctl */
#include <unistd.h> /* for close */
#include </usr/include/linux/fs.h> /* for BLKFLSBUF */

#include "internal_disk.h"

#define SG_READ			0
#define	EINVAL		22	/* Invalid argument */
#define SG_ATA_16_LEN		16
#define SG_ATA_16		0x85
#define SG_ATA_12		0xa1
#define SG_ATA_12_LEN		12

#define SG_ATA_LBA48		1
#define SG_ATA_PROTO_NON_DATA	( 3 << 1)
#define SG_ATA_PROTO_PIO_IN	( 4 << 1)
#define SG_ATA_PROTO_PIO_OUT	( 5 << 1)
#define SG_ATA_PROTO_DMA	( 6 << 1)

#define SG_CHECK_CONDITION	0x02
#define SG_DRIVER_SENSE		0x08

#define SG_PIO			0
#define SG_DMA			1

#define lba28_limit ((__u64)(1<<28) - 1)
#define __le16_to_cpus(x) do { (void)(x); } while (0)

enum {
	ATA_OP_DSM = 0x06, // Data Set Management (TRIM)
	ATA_OP_READ_PIO = 0x20,
	ATA_OP_READ_PIO_ONCE = 0x21,
	ATA_OP_READ_LONG = 0x22,
	ATA_OP_READ_LONG_ONCE = 0x23,
	ATA_OP_READ_PIO_EXT = 0x24,
	ATA_OP_READ_DMA_EXT = 0x25,
	ATA_OP_READ_FPDMA = 0x60, // NCQ
	ATA_OP_WRITE_PIO = 0x30,
	ATA_OP_WRITE_LONG = 0x32,
	ATA_OP_WRITE_LONG_ONCE = 0x33,
	ATA_OP_WRITE_PIO_EXT = 0x34,
	ATA_OP_WRITE_DMA_EXT = 0x35,
	ATA_OP_WRITE_FPDMA = 0x61, // NCQ
	ATA_OP_READ_VERIFY = 0x40,
	ATA_OP_READ_VERIFY_ONCE = 0x41,
	ATA_OP_READ_VERIFY_EXT = 0x42,
	ATA_OP_WRITE_UNC_EXT = 0x45, // lba48, no data, uses feat reg
	ATA_OP_FORMAT_TRACK = 0x50,
	ATA_OP_DOWNLOAD_MICROCODE = 0x92,
	ATA_OP_STANDBYNOW2 = 0x94,
	ATA_OP_CHECKPOWERMODE2 = 0x98,
	ATA_OP_SLEEPNOW2 = 0x99,
	ATA_OP_PIDENTIFY = 0xa1,
	ATA_OP_READ_NATIVE_MAX = 0xf8,
	ATA_OP_READ_NATIVE_MAX_EXT = 0x27,
	ATA_OP_SMART = 0xb0,
	ATA_OP_DCO = 0xb1,
	ATA_OP_ERASE_SECTORS = 0xc0,
	ATA_OP_READ_DMA = 0xc8,
	ATA_OP_WRITE_DMA = 0xca,
	ATA_OP_DOORLOCK = 0xde,
	ATA_OP_DOORUNLOCK = 0xdf,
	ATA_OP_STANDBYNOW1 = 0xe0,
	ATA_OP_IDLEIMMEDIATE = 0xe1,
	ATA_OP_SETIDLE = 0xe3,
	ATA_OP_SET_MAX = 0xf9,
	ATA_OP_SET_MAX_EXT = 0x37,
	ATA_OP_SET_MULTIPLE = 0xc6,
	ATA_OP_CHECKPOWERMODE1 = 0xe5,
	ATA_OP_SLEEPNOW1 = 0xe6,
	ATA_OP_FLUSHCACHE = 0xe7,
	ATA_OP_FLUSHCACHE_EXT = 0xea,
	ATA_OP_IDENTIFY = 0xec,
	ATA_OP_SETFEATURES = 0xef,
	ATA_OP_SECURITY_SET_PASS = 0xf1,
	ATA_OP_SECURITY_UNLOCK = 0xf2,
	ATA_OP_SECURITY_ERASE_PREPARE = 0xf3,
	ATA_OP_SECURITY_ERASE_UNIT = 0xf4,
	ATA_OP_SECURITY_FREEZE_LOCK = 0xf5,
	ATA_OP_SECURITY_DISABLE = 0xf6,
	HDIO_DRIVE_CMD = 0x031f,
};

/*
 * Some useful ATA register bits
 */
enum {
	ATA_USING_LBA = (1 << 6), ATA_STAT_DRQ = (1 << 3), ATA_STAT_ERR = (1 << 0),
};

enum {
	SG_CDB2_TLEN_NODATA = 0 << 0,
	SG_CDB2_TLEN_FEAT = 1 << 0,
	SG_CDB2_TLEN_NSECT = 2 << 0,

	SG_CDB2_TLEN_BYTES = 0 << 2,
	SG_CDB2_TLEN_SECTORS = 1 << 2,

	SG_CDB2_TDIR_TO_DEV = 0 << 3,
	SG_CDB2_TDIR_FROM_DEV = 1 << 3,

	SG_CDB2_CHECK_COND = 1 << 5,
};

/*
 * Definitions and structures for use with SG_IO + ATA_16:
 */
struct ata_lba_regs {
	__u8 feat;
	__u8 nsect;
	__u8 lbal;
	__u8 lbam;
	__u8 lbah;
};

struct ata_tf {
	__u8 dev;
	__u8 command;
	__u8 error;
	__u8 status;
	__u8 is_lba48;
	struct ata_lba_regs lob;
	struct ata_lba_regs hob;
};

struct scsi_sg_io_hdr {
	int interface_id;
	int dxfer_direction;
	unsigned char cmd_len;
	unsigned char mx_sb_len;
	unsigned short iovec_count;
	unsigned int dxfer_len;
	void * dxferp;
	unsigned char * cmdp;
	void * sbp;
	unsigned int timeout;
	unsigned int flags;
	int pack_id;
	void * usr_ptr;
	unsigned char status;
	unsigned char masked_status;
	unsigned char msg_status;
	unsigned char sb_len_wr;
	unsigned short host_status;
	unsigned short driver_status;
	int resid;
	unsigned int duration;
	unsigned int info;
};

int prefer_ata12 = 0;
static const int default_timeout_secs = 15;
int id_fd;

static void dump_bytes(const char *prefix, unsigned char *p, int len) {
	int i;

	if (prefix)
		fprintf(stderr, "%s: ", prefix);
	for (i = 0; i < len; ++i)
		fprintf(stderr, " %02x", p[i]);
	fprintf(stderr, "\n");
}

static inline int is_dma(__u8 ata_op) {
	switch (ata_op) {
	case ATA_OP_DSM:
	case ATA_OP_READ_DMA_EXT:
	case ATA_OP_READ_FPDMA:
	case ATA_OP_WRITE_DMA_EXT:
	case ATA_OP_WRITE_FPDMA:
	case ATA_OP_READ_DMA:
	case ATA_OP_WRITE_DMA:
		return SG_DMA;
	default:
		return SG_PIO;
	}
}

static inline int needs_lba48(__u8 ata_op, __u64 lba, unsigned int nsect) {
	switch (ata_op) {
	case ATA_OP_DSM:
	case ATA_OP_READ_PIO_EXT:
	case ATA_OP_READ_DMA_EXT:
	case ATA_OP_WRITE_PIO_EXT:
	case ATA_OP_WRITE_DMA_EXT:
	case ATA_OP_READ_VERIFY_EXT:
	case ATA_OP_WRITE_UNC_EXT:
	case ATA_OP_READ_NATIVE_MAX_EXT:
	case ATA_OP_SET_MAX_EXT:
	case ATA_OP_FLUSHCACHE_EXT:
		return 1;
	case ATA_OP_SECURITY_ERASE_PREPARE:
	case ATA_OP_SECURITY_ERASE_UNIT:
		return 0;
	}
	if (lba >= lba28_limit)
		return 1;
	if (nsect) {
		if (nsect > 0xff)
			return 1;
		if ((lba + nsect - 1) >= lba28_limit)
			return 1;
	}
	return 0;
}

__u64 tf_to_lba(struct ata_tf *tf) {
	__u32 lba24, lbah;
	__u64 lba64;

	lba24 = (tf->lob.lbah << 16) | (tf->lob.lbam << 8) | (tf->lob.lbal);
	if (tf->is_lba48)
		lbah = (tf->hob.lbah << 16) | (tf->hob.lbam << 8) | (tf->hob.lbal);
	else
		lbah = (tf->dev & 0x0f);
	lba64 = (((__u64 ) lbah) << 24) | (__u64 ) lba24;
	return lba64;
}

void tf_init(struct ata_tf *tf, __u8 ata_op, __u64 lba, unsigned int nsect) {
	memset(tf, 0, sizeof(*tf));
	tf->command = ata_op;
	tf->dev = ATA_USING_LBA;
	tf->lob.lbal = lba;
	tf->lob.lbam = lba >> 8;
	tf->lob.lbah = lba >> 16;
	tf->lob.nsect = nsect;
	if (needs_lba48(ata_op, lba, nsect)) {
		tf->is_lba48 = 1;
		tf->hob.nsect = nsect >> 8;
		tf->hob.lbal = lba >> 24;
		tf->hob.lbam = lba >> 32;
		tf->hob.lbah = lba >> 40;
	} else {
		tf->dev |= (lba >> 24) & 0x0f;
	}
}

int sg16(int fd, int rw, int dma, struct ata_tf *tf, void *data,
		unsigned int data_bytes, unsigned int timeout_secs) {
	unsigned char cdb[SG_ATA_16_LEN];
	unsigned char sb[32], *desc;
	struct scsi_sg_io_hdr io_hdr;
	int prefer12 = prefer_ata12, demanded_sense = 0;

	if (tf->command == ATA_OP_PIDENTIFY)
		prefer12 = 0;

	memset(&cdb, 0, sizeof(cdb));
	memset(&sb, 0, sizeof(sb));
	memset(&io_hdr, 0, sizeof(struct scsi_sg_io_hdr));
	if (data && data_bytes && !rw)
		memset(data, 0, data_bytes);

	if (dma) {
		//cdb[1] = data ? (rw ? SG_ATA_PROTO_UDMA_OUT : SG_ATA_PROTO_UDMA_IN) : SG_ATA_PROTO_NON_DATA;
		cdb[1] = data ? SG_ATA_PROTO_DMA : SG_ATA_PROTO_NON_DATA;
	} else {
		cdb[1] = data ? (rw ? SG_ATA_PROTO_PIO_OUT : SG_ATA_PROTO_PIO_IN)
				: SG_ATA_PROTO_NON_DATA;
	}

	/* libata/AHCI workaround: don't demand sense data for IDENTIFY commands */
	if (data) {
		cdb[2] |= SG_CDB2_TLEN_NSECT | SG_CDB2_TLEN_SECTORS;
		cdb[2] |= rw ? SG_CDB2_TDIR_TO_DEV : SG_CDB2_TDIR_FROM_DEV;
	} else {
		cdb[2] = SG_CDB2_CHECK_COND;
	}

	if (!prefer12 || tf->is_lba48) {
		cdb[0] = SG_ATA_16;
		cdb[4] = tf->lob.feat;
		cdb[6] = tf->lob.nsect;
		cdb[8] = tf->lob.lbal;
		cdb[10] = tf->lob.lbam;
		cdb[12] = tf->lob.lbah;
		cdb[13] = tf->dev;
		cdb[14] = tf->command;
		if (tf->is_lba48) {
			cdb[1] |= SG_ATA_LBA48;
			cdb[3] = tf->hob.feat;
			cdb[5] = tf->hob.nsect;
			cdb[7] = tf->hob.lbal;
			cdb[9] = tf->hob.lbam;
			cdb[11] = tf->hob.lbah;
		}
		io_hdr.cmd_len = SG_ATA_16_LEN;
	} else {
		cdb[0] = SG_ATA_12;
		cdb[3] = tf->lob.feat;
		cdb[4] = tf->lob.nsect;
		cdb[5] = tf->lob.lbal;
		cdb[6] = tf->lob.lbam;
		cdb[7] = tf->lob.lbah;
		cdb[8] = tf->dev;
		cdb[9] = tf->command;
		io_hdr.cmd_len = SG_ATA_12_LEN;
	}

	io_hdr.interface_id = 'S';
	io_hdr.mx_sb_len = sizeof(sb);
	io_hdr.dxfer_direction = data ? (rw ? SG_DXFER_TO_DEV : SG_DXFER_FROM_DEV)
			: SG_DXFER_NONE;
	io_hdr.dxfer_len = data ? data_bytes : 0;
	io_hdr.dxferp = data;
	io_hdr.cmdp = cdb;
	io_hdr.sbp = sb;
	io_hdr.pack_id = tf_to_lba(tf);
	io_hdr.timeout = (timeout_secs ? timeout_secs : default_timeout_secs)
			* 1000; /* msecs */

//	dump_bytes("outgoing cdb", cdb, sizeof(cdb));
//	if (rw && data)
//		dump_bytes("outgoing_data", data, data_bytes);

	if (ioctl(fd, SG_IO, &io_hdr) == -1) {
		printf("ioctl(fd,SG_IO)");
		return -1; /* SG_IO not supported */
	}

//	fprintf(
//			stderr,
//			"SG_IO: ATA_%u status=0x%x, host_status=0x%x, driver_status=0x%x\n",
//			io_hdr.cmd_len, io_hdr.status, io_hdr.host_status,
//			io_hdr.driver_status);

	if (io_hdr.status && io_hdr.status != SG_CHECK_CONDITION) {
		fprintf(stderr, "SG_IO: bad status: 0x%x\n", io_hdr.status);
		errno = EBADE;
		return -1;
	}
	if (io_hdr.host_status) {
		fprintf(stderr, "SG_IO: bad host status: 0x%x\n", io_hdr.host_status);
		errno = EBADE;
		return -1;
	}
//	dump_bytes("SG_IO: sb[]", sb, sizeof(sb));
//	if (!rw && data)
//		dump_bytes("incoming_data", data, data_bytes);

	if (io_hdr.driver_status && (io_hdr.driver_status != SG_DRIVER_SENSE)) {
		fprintf(stderr, "SG_IO: bad driver status: 0x%x\n",
				io_hdr.driver_status);
		errno = EBADE;
		return -1;
	}

	desc = sb + 8;
	if (io_hdr.driver_status != SG_DRIVER_SENSE) {
		if (sb[0] | sb[1] | sb[2] | sb[3] | sb[4] | sb[5] | sb[6] | sb[7]
				| sb[8] | sb[9]) {
			static int second_try = 0;
			if (!second_try++)
				fprintf(stderr,
						"SG_IO: questionable sense data, results may be incorrect\n");
		} else if (demanded_sense) {
			static int second_try = 0;
			if (!second_try++)
				fprintf(stderr,
						"SG_IO: missing sense data, results may be incorrect\n");
		}
	} else if (sb[0] != 0x72 || sb[7] < 14 || desc[0] != 0x09 || desc[1] < 0x0c) {
		dump_bytes("SG_IO: bad/missing sense data, sb[]", sb, sizeof(sb));
	}

	unsigned int len = desc[1] + 2, maxlen = sizeof(sb) - 8 - 2;
	if (len > maxlen)
		len = maxlen;
	//dump_bytes("SG_IO: desc[]", desc, len);

	tf->is_lba48 = desc[2] & 1;
	tf->error = desc[3];
	tf->lob.nsect = desc[5];
	tf->lob.lbal = desc[7];
	tf->lob.lbam = desc[9];
	tf->lob.lbah = desc[11];
	tf->dev = desc[12];
	tf->status = desc[13];
	tf->hob.feat = 0;
	if (tf->is_lba48) {
		tf->hob.nsect = desc[4];
		tf->hob.lbal = desc[6];
		tf->hob.lbam = desc[8];
		tf->hob.lbah = desc[10];
	} else {
		tf->hob.nsect = 0;
		tf->hob.lbal = 0;
		tf->hob.lbam = 0;
		tf->hob.lbah = 0;
	}

//	fprintf(
//			stderr,
//			"      ATA_%u stat=%02x err=%02x nsect=%02x lbal=%02x lbam=%02x lbah=%02x dev=%02x\n",
//			io_hdr.cmd_len, tf->status, tf->error, tf->lob.nsect, tf->lob.lbal,
//			tf->lob.lbam, tf->lob.lbah, tf->dev);

	if (tf->status & (ATA_STAT_ERR | ATA_STAT_DRQ)) {

		fprintf(
				stderr,
				"I/O error, ata_op=0x%02x ata_status=0x%02x ata_error=0x%02x\n",
				tf->command, tf->status, tf->error);
		errno = EIO;
		return -1;
	}
	return 0;
}



int do_drive_cmd(int fd, unsigned char *args, unsigned int timeout_secs) {
#ifdef SG_IO

	struct ata_tf tf;
	void *data = NULL;
	unsigned int data_bytes = 0;
	int rc;

	if (args == NULL)
		goto use_legacy_ioctl;
	/*
	 * Reformat and try to issue via SG_IO:
	 * args[0]: command in; status out.
	 * args[1]: lbal for SMART, nsect for all others; error out
	 * args[2]: feat in; nsect out.
	 * args[3]: data-count (512 multiple) for all cmds.
	 */
	tf_init(&tf, args[0], 0, 0);
	tf.lob.nsect = args[1];
	tf.lob.feat = args[2];
	if (args[3]) {
		data_bytes = args[3] * 512;
		data = args + 4;
		if (!tf.lob.nsect)
			tf.lob.nsect = args[3];
	}
	if (tf.command == ATA_OP_SMART) {
		tf.lob.nsect = args[3];
		tf.lob.lbal = args[1];
		tf.lob.lbam = 0x4f;
		tf.lob.lbah = 0xc2;
	}

	rc = sg16(fd, SG_READ, is_dma(tf.command), &tf, data, data_bytes,
			timeout_secs);
	if (rc == -1) {
		if (errno == EINVAL || errno == ENODEV || errno == EBADE)
			goto use_legacy_ioctl;
	}

	if (rc == 0 || errno == EIO) {
		args[0] = tf.status;
		args[1] = tf.error;
		args[2] = tf.lob.nsect;
	}
	return rc;

	use_legacy_ioctl:
#endif /* SG_IO */

	if (args)
		fprintf(stderr, "Trying legacy HDIO_DRIVE_CMD\n");

	return ioctl(fd, HDIO_DRIVE_CMD, args);
}

static __u8 last_identify_op = 0;
static __u16 *id;

static void get_identify_data (int fd)
{
	static __u8 args[4+512];
	int i;

	if (id)
		return;
	memset(args, 0, sizeof(args));
	last_identify_op = ATA_OP_IDENTIFY;
	args[0] = last_identify_op;
	args[3] = 1;	/* sector count */
	if (do_drive_cmd(fd, args, 0)) {
		prefer_ata12 = 0;
		memset(args, 0, sizeof(args));
		last_identify_op = ATA_OP_PIDENTIFY;
		args[0] = last_identify_op;
		args[3] = 1;	/* sector count */
		if (do_drive_cmd(fd, args, 0)) {
			perror(" HDIO_DRIVE_CMD(identify) failed");
			return;
		}
	}
	/* byte-swap the little-endian IDENTIFY data to match byte-order on host
 CPU */
	id = (void *)(args + 4);
	for (i = 0; i < 0x100; ++i)
		__le16_to_cpus(&id[i]);
}

static void flush_buffer_cache (int fd)
{
	sync();
	fsync(fd);				/* flush buffers */
	fdatasync(fd);				/* flush buffers */
	sync();
	if (ioctl(fd, BLKFLSBUF, NULL))		/* do it again, big time */
		perror("BLKFLSBUF failed");
	else
		do_drive_cmd(fd, NULL, 0);	/* IDE: await completion */
	sync();
}

static int flush_wcache (int fd)
{
	__u8 args[4] = {ATA_OP_FLUSHCACHE,0,0,0};
	int err = 0;

	get_identify_data(fd);
	if (id && (id[83] & 0xe000) == 0x6000)
		args[0] = ATA_OP_FLUSHCACHE_EXT;
	if (do_drive_cmd(fd, args, 60)) {
		err = errno;
		perror (" HDIO_DRIVE_CMD(flushcache) failed");
	}
	return err;
}

int id_open(const char * devname) {
	static int open_flags = O_RDONLY | O_NONBLOCK;

	id_fd = open(devname, open_flags);
	if (id_fd < 0) {
		return 1;
	}

	return 0;
}

void id_close() {
	close(id_fd);
}

int id_flush() {
	flush_buffer_cache(id_fd); /* -F   Flush drive write cache */
	int ret = flush_wcache(id_fd); /* -f   Flush buffer cache for device on exit */

	return ret;
}

int id_put_drive_to_sleep() {
	/* -Y */

	__u8 args3[4] = { ATA_OP_SLEEPNOW1, 0, 0, 0 };
	__u8 args4[4] = { ATA_OP_SLEEPNOW2, 0, 0, 0 };

	if (do_drive_cmd(id_fd, args3, 0) && do_drive_cmd(id_fd, args4, 0)) {
		printf(" HDIO_DRIVE_CMD(sleep) failed");
		return 3;
	}

	return 0;
}

int id_put_drive_in_standby() {
	/* -y */

	__u8 args1[4] = { ATA_OP_STANDBYNOW1, 0, 0, 0 };
	__u8 args2[4] = { ATA_OP_STANDBYNOW2, 0, 0, 0 };

	if (do_drive_cmd(id_fd, args1, 0) && do_drive_cmd(id_fd, args2, 0)) {
		printf(" HDIO_DRIVE_CMD(standby) failed");
		return 2;
	}

	return 0;
}

int id_check_drive_power_mode_status() {
	/* -C */

	__u8 args[4] = { ATA_OP_CHECKPOWERMODE1, 0, 0, 0 };
	const char *state = "unknown";
	if (do_drive_cmd(id_fd, args, 0) && (args[0] = ATA_OP_CHECKPOWERMODE2) /* (single =) try again with 0x98 */
	&& do_drive_cmd(id_fd, args, 0)) {
		return STATE_UNKNOWN;
	} else {
		switch (args[2]) {
		case 0x00:
			return STATE_STANDBY;
		case 0x40:
			return STATE_SPINDOWN;
		case 0x41:
			return STATE_SPINUP;
		case 0x80:
			return STATE_IDLE;
		case 0xff:
			return STATE_ACTIVE_IDLE;
		}
	}

	return STATE_UNKNOWN;
}
